package weshare.server;

import weshare.controller.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes {
    public static final String LOGIN_PAGE = "/";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = "/logout";

    public static final String EXPENSES = "/expenses";
    public static final String NEW_EXPENSE = "/newexpense";
    public static final String CREATE_EXPENSE = "/expense.action";

    public static final String PAYMENT_REQUEST = "/paymentrequest";
    public static final String PAYMENT_REQUESTS_RECEIVED = "/paymentrequests_received";
    public static final String PAYMENT_REQUEST_SENT = "/paymentrequests_sent";
    public static final String PAYMENT_REQUEST_ACTION = "/paymentrequest.action";

    public static final String PAYMENT_ACTION = "/payment.action";

    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION,                  PersonController.login);
            get(LOGOUT,                         PersonController.logout);
            get(EXPENSES,                       ExpensesController.view);
            get(NEW_EXPENSE,                    ExpensesController.newExpense);
            post(CREATE_EXPENSE,                ExpensesController.expenseAction);
            get(PAYMENT_REQUEST,                PaymentRequestsController.paymentRequestView);
            get(PAYMENT_REQUESTS_RECEIVED,      PaymentRequestsController.paymentRequestReceivedView);
            get(PAYMENT_REQUEST_SENT,           PaymentRequestsController.paymentRequestSentView);
            post(PAYMENT_REQUEST_ACTION,        PaymentRequestsController.paymentRequest);
            post(PAYMENT_ACTION,                PaymentRequestsController.payment);
        });
    }
}
