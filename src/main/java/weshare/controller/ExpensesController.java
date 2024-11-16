package weshare.controller;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.function.MonetaryFunctions;
import org.jetbrains.annotations.NotNull;
import weshare.model.Expense;
import weshare.model.MoneyHelper;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.server.Routes;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static weshare.model.MoneyHelper.ZERO_RANDS;

public class ExpensesController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);

        MonetaryAmount total = calculateTotalExpense(expenses);

        boolean NoUnpaidExpense = total.equals(ZERO_RANDS);

        Map<String, Object> viewModel = new HashMap<>();
        if ( NoUnpaidExpense ) {
            viewModel = Map.of( "expenses", Collections.emptyList() );
        } else {
            viewModel = Map.of(
                    "expenses", expenses,
                    "totalExpense", total,
                    "allExpensesPaid", NoUnpaidExpense
            );
        }

        context.render("expenses.html", viewModel);
    };

    public static final Handler newExpense = context -> {
        context.render("newexpense.html");
    };

    private static MonetaryAmount calculateTotalExpense(Collection<Expense> expenses) {
        MonetaryAmount total = MoneyHelper.amountOf(0);

        for (Expense expense : expenses) {
            total = total.add(expense.amountLessPaymentsReceived());
        } return total;
    }

    public static final Handler expenseAction = context -> {
        ExpenseDAO expenseDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person loggedInAcc = WeShareServer.getPersonLoggedIn(context);

        String description = context.formParamMap().get( "description" ).get(0);
        MonetaryAmount amount = MoneyHelper.amountOf( Integer.parseInt( context.formParamMap().get( "amount" ).get(0) ) );
        String date = context.formParamMap().get( "date" ).get(0);
        LocalDate newDate = LocalDate.parse( date );

        Expense expense = new Expense( loggedInAcc, description, amount, newDate );
        expenseDAO.save( expense );

        context.redirect( Routes.EXPENSES );
    };
}
