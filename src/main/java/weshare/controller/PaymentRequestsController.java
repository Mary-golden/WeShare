package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.*;
import weshare.persistence.ExpenseDAO;
import weshare.persistence.PersonDAO;
import weshare.server.Routes;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentRequestsController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final Handler paymentRequestSentView = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Collection<PaymentRequest> paymentRequestsSent = expensesDAO.findPaymentRequestsSent(personLoggedIn);
        MonetaryAmount total = calculateTotal(paymentRequestsSent);

        Map<String, Object> viewModel = Map.of("paymentRequest", paymentRequestsSent, "totalUnpaid", total);
        context.render("paymentrequests_sent.html", viewModel);
    };

    public static final Handler paymentRequestReceivedView = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Collection<PaymentRequest> paymentRequestsReceived = expensesDAO.findPaymentRequestsReceived(personLoggedIn);
        MonetaryAmount total = calculateTotal(paymentRequestsReceived);

        Map<String, Object> viewModel = Map.of("paymentRequest", paymentRequestsReceived, "totalUnpaid", total);
        context.render("paymentrequests_received.html", viewModel);
    };

    public static final Handler paymentRequestView = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        UUID expenseId = UUID.fromString(context.queryParam("expenseId"));
        Expense expense = expensesDAO.get(expenseId).orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        List<PaymentRequest> paymentRequestList = expensesDAO.findPaymentRequestsSent(personLoggedIn).stream()
                .filter(pr -> pr.getExpense().equals(expense))
                .collect(Collectors.toList());

        MonetaryAmount total = calculateTotal(paymentRequestList);

        Map<String, Object> viewModel = Map.of("expense", expense, "paymentRequestList", paymentRequestList, "grandTotal", total);
        context.render("paymentrequest.html", viewModel);
    };

    public static final Handler paymentRequest = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);

        UUID expenseId = UUID.fromString(context.formParam("expense_id"));
        Expense expense = expensesDAO.get(expenseId).orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        Person person = personDAO.savePerson(new Person(context.formParam("email")));
        MonetaryAmount amount = MoneyHelper.amountOf(Integer.parseInt(context.formParam("amount")));
        LocalDate dueDate = LocalDate.parse(context.formParam("due_date"), DATE_FORMATTER);

        expense.requestPayment(person, amount, dueDate);
        expensesDAO.save(expense);

        context.redirect(Routes.PAYMENT_REQUEST + "?expenseId=" + expenseId);
    };

    public static final Handler payment = context -> {
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        UUID paymentId = UUID.fromString(context.formParam("paymentId"));

        expensesDAO.findPaymentRequestsReceived(personLoggedIn).stream()
                .filter(pr -> pr.getId().equals(paymentId))
                .findFirst()
                .ifPresent(paymentRequest -> {
                    Payment payment = paymentRequest.pay(personLoggedIn, LocalDate.now());
                    Expense expense = payment.getExpenseForPersonPaying();
                    expensesDAO.save(expense);
                });

        context.redirect(Routes.PAYMENT_REQUESTS_RECEIVED);
    };

    private static MonetaryAmount calculateTotal(Collection<PaymentRequest> paymentRequests) {
        MonetaryAmount total = MoneyHelper.amountOf(0);

        for (PaymentRequest payReq : paymentRequests) {
            total = total.add(payReq.getAmountToPay());
        } return total;
    }
}