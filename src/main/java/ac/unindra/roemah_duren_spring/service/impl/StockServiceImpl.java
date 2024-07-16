package ac.unindra.roemah_duren_spring.service.impl;

import ac.unindra.roemah_duren_spring.constant.ConstantAPIUrl;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.dto.request.StockRequest;
import ac.unindra.roemah_duren_spring.dto.response.CommonResponse;
import ac.unindra.roemah_duren_spring.dto.response.ProductResponse;
import ac.unindra.roemah_duren_spring.dto.response.StockResponse;
import ac.unindra.roemah_duren_spring.model.Page;
import ac.unindra.roemah_duren_spring.model.Stock;
import ac.unindra.roemah_duren_spring.service.StockService;
import ac.unindra.roemah_duren_spring.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final WebClientUtil webClientUtil;

    @Override
    public void createStock(Stock stock, WebClientUtil.SuccessCallback<Void> successCallback, WebClientUtil.ErrorCallback errorCallback) {
        StockRequest request = StockRequest.builder()
                .productId(stock.getProduct().getId())
                .stock(stock.getStock())
                .branchId(stock.getBranch().getId())
                .build();
        webClientUtil.post(
                ConstantAPIUrl.StockAPI.BASE_URL,
                request,
                new ParameterizedTypeReference<CommonResponse<Void>>() {
                },
                response -> successCallback.onSuccess(null),
                errorCallback
        );
    }

    @Override
    public void getStocks(QueryRequest request, WebClientUtil.SuccessCallback<Page<Stock>> successCallback, WebClientUtil.ErrorCallback errorCallback) {
        Map<String, String> query = Map.of(
                "page", String.valueOf(request.getPage()),
                "size", String.valueOf(request.getSize()),
                "q", request.getQuery()
        );

        getAll(successCallback, errorCallback, query);
    }

    @Override
    public void getAllStocks(WebClientUtil.SuccessCallback<Page<Stock>> successCallback, WebClientUtil.ErrorCallback errorCallback) {
        getAll(successCallback, errorCallback, Map.of(
                "all", "true"
        ));
    }

    @Override
    public void updateStock(Stock stock, WebClientUtil.SuccessCallback<Void> successCallback, WebClientUtil.ErrorCallback errorCallback) {
        StockRequest request = StockRequest.builder()
                .id(stock.getId())
                .productId(stock.getProduct().getId())
                .stock(stock.getStock())
                .branchId(stock.getBranch().getId())
                .build();
        webClientUtil.put(
                ConstantAPIUrl.StockAPI.BASE_URL,
                request,
                new ParameterizedTypeReference<CommonResponse<Void>>() {
                },
                response -> successCallback.onSuccess(null),
                errorCallback
        );
    }

    @Override
    public void deleteStock(String id, WebClientUtil.SuccessCallback<Void> successCallback, WebClientUtil.ErrorCallback errorCallback) {
        webClientUtil.delete(
                ConstantAPIUrl.StockAPI.GET_BASE_URL_WITH_ID(id),
                new ParameterizedTypeReference<CommonResponse<Void>>() {
                },
                response -> successCallback.onSuccess(null),
                errorCallback
        );
    }

    private void getAll(WebClientUtil.SuccessCallback<Page<Stock>> successCallback, WebClientUtil.ErrorCallback errorCallback, Map<String, String> query) {
        webClientUtil.getWithQueryParam(
                ConstantAPIUrl.StockAPI.BASE_URL,
                new ParameterizedTypeReference<CommonResponse<List<StockResponse>>>() {
                },
                query,
                response -> {
                    List<Stock> stocks = response.getData().stream().map(StockResponse::toStock).toList();
                    Page<Stock> page = Page.<Stock>builder()
                            .data(stocks)
                            .paging(response.getPaging())
                            .build();
                    successCallback.onSuccess(page);
                },
                errorCallback
        );
    }
}
