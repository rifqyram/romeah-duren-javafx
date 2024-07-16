package ac.unindra.roemah_duren_spring.service;

import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.model.Page;
import ac.unindra.roemah_duren_spring.model.Stock;
import ac.unindra.roemah_duren_spring.util.WebClientUtil;

public interface StockService {
    void createStock(Stock stock, WebClientUtil.SuccessCallback<Void> successCallback, WebClientUtil.ErrorCallback errorCallback);
    void getStocks(QueryRequest request, WebClientUtil.SuccessCallback<Page<Stock>> successCallback, WebClientUtil.ErrorCallback errorCallback);
    void getAllStocks(WebClientUtil.SuccessCallback<Page<Stock>> successCallback, WebClientUtil.ErrorCallback errorCallback);
    void updateStock(Stock stock, WebClientUtil.SuccessCallback<Void> successCallback, WebClientUtil.ErrorCallback errorCallback);
    void deleteStock(String id, WebClientUtil.SuccessCallback<Void> successCallback, WebClientUtil.ErrorCallback errorCallback);
}
