package io.vertx.workshop.portfolio.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.workshop.portfolio.Portfolio;
import io.vertx.workshop.portfolio.PortfolioService;

/**
 * The portfolio service implementation.
 */
public class PortfolioServiceImpl implements PortfolioService {

    private final Vertx vertx;
    private final Portfolio portfolio;
    private final ServiceDiscovery discovery;

    public PortfolioServiceImpl(Vertx vertx, ServiceDiscovery discovery, double initialCash) {
        this.vertx = vertx;
        this.portfolio = new Portfolio().setCash(initialCash);
        this.discovery = discovery;
    }

    @Override
    public void getPortfolio(Handler<AsyncResult<Portfolio>> resultHandler) {
        // TODO Call the given handler with a successful Async Result encapsulating the
        // `portfolio` object. The async result instance is created using
        // `Future.succeededFuture`
        // ---
        resultHandler.handle(Future.succeededFuture(portfolio));
        // ---

    }

    private void sendActionOnTheEventBus(String action, int amount, JsonObject quote, int newAmount) {
        // TODO Broadcast a Json message to the `EVENT_ADDRESS` containing the
        // following keys: "action", "quote", "date"
        // (use System.currentTimeMillis()), "amount" and "owned" (newAmount)
        // ---

        vertx.eventBus().publish(EVENT_ADDRESS, 
                new JsonObject()
                    .put("action", action)
                    .put("quote", quote)
                    .put("date", System.currentTimeMillis())
                    .put("amount", amount)
                    .put("owned", newAmount)
        );

        // ---
    }

    @Override
    public void evaluate(Handler<AsyncResult<Double>> resultHandler) {
        // TODO
        // ---

        Single<WebClient> quotes = HttpEndpoint.rxGetWebClient(discovery,
            service -> service.getName().equals("quote-generator"));
        quotes.subscribe((client, err) -> {
            if (err != null) {
                resultHandler.handle(Future.failedFuture(err));
            } else {
                computeEvaluation(client, resultHandler);
            }
        });

        // ---

    }

    @Override
    public void buy(int amount, JsonObject quote, Handler<AsyncResult<Portfolio>> resultHandler) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sell(int amount, JsonObject quote, Handler<AsyncResult<Portfolio>> resultHandler) {
        // TODO Auto-generated method stub

    }

    private void computeEvaluation(WebClient client, Handler<AsyncResult<Double>> resultHandler) {

        // We need to call the service for each company in which we own numberOfShares
        Flowable.fromIterable(portfolio.getShares().entrySet())
                // For each we retrieve the valule
                .flatMapSingle(entry -> getValueForCompany(client, entry.getKey(), entry.getValue()))
                // We accumulate the results until the end of the stream
                .toList()
                // And compute the sum
                .map(list -> list.stream().mapToDouble(x -> x).sum())
                // We report the result or failure
                .subscribe((sum, err) -> {
                    if (err != null) {
                        System.out.println("Evaluation of the portfolio failed " + err.getMessage());
                        resultHandler.handle(Future.failedFuture(err));
                    } else {
                        System.out.println("Evaluation of the portfolio succeeded");
                        resultHandler.handle(Future.succeededFuture(sum));
                    }
                });
    }

    private Single<Double> getValueForCompany(WebClient client, String company, Integer numberOfShares) {

        return client.get("/?name=" + encode(company))
                .as(BodyCodec.jsonObject())
                .rxSend()
                .map(HttpResponse::body)
                .map(json -> json.getDouble("bid"))
                .map(val -> val * numberOfShares);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding");
        }

    }
    
    
}