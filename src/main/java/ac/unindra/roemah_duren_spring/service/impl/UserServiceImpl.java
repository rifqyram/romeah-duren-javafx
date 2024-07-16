package ac.unindra.roemah_duren_spring.service.impl;

import ac.unindra.roemah_duren_spring.constant.ConstantAPIUrl;
import ac.unindra.roemah_duren_spring.dto.request.QueryRequest;
import ac.unindra.roemah_duren_spring.dto.response.CommonResponse;
import ac.unindra.roemah_duren_spring.dto.response.UserResponse;
import ac.unindra.roemah_duren_spring.service.UserService;
import ac.unindra.roemah_duren_spring.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final WebClientUtil webClientUtil;

    @Override
    public void getUserInfoByToken(WebClientUtil.SuccessCallback<UserResponse> successCallback, WebClientUtil.ErrorCallback errorCallback) {
        webClientUtil.get(
                ConstantAPIUrl.UserAPI.BASE_URL,
                new ParameterizedTypeReference<CommonResponse<UserResponse>>() {
                },
                response -> successCallback.onSuccess(response.getData()),
                errorCallback
        );
    }
}
