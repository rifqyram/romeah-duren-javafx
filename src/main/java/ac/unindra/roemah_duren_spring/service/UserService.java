package ac.unindra.roemah_duren_spring.service;

import ac.unindra.roemah_duren_spring.dto.response.UserResponse;
import ac.unindra.roemah_duren_spring.util.WebClientUtil;

public interface UserService {
    void getUserInfoByToken(WebClientUtil.SuccessCallback<UserResponse> successCallback, WebClientUtil.ErrorCallback errorCallback);
}
