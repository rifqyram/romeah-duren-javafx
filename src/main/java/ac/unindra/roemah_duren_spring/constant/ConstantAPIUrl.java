package ac.unindra.roemah_duren_spring.constant;

import ac.unindra.roemah_duren_spring.dto.request.ReportQueryRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConstantAPIUrl {
    public static String BASE_URL;

    @Value("${app.roemah-duren.base-url-api}")
    public void setBaseUrl(String baseUrl) {
        ConstantAPIUrl.BASE_URL = baseUrl;
    }

    public static class UserAPI {
        public static final String BASE_URL = "/users";
    }

    public static class AuthAPI {
        public static final String LOGIN = "/auth/login";
        public static final String REGISTER = "/auth/register";
    }

    public static class BranchAPI {
        public static final String BASE_URL = "/branches";
        private static final String BASE_URL_WITH_ID = "/branches/{id}";

        public static String GET_BASE_URL_WITH_ID(String id) {
            return BASE_URL_WITH_ID.replace("{id}", id);
        }

    }

    public static class SupplierAPI {

        public static final String BASE_URL = "/suppliers";
        private static final String BASE_URL_WITH_ID = "/suppliers/{id}";
        public static String GET_BASE_URL_WITH_ID(String id) {
            return BASE_URL_WITH_ID.replace("{id}", id);
        }
    }

    public static class CustomerAPI {

        public static final String BASE_URL = "/customers";
        private static final String BASE_URL_WITH_ID = "/customers/{id}";
        public static String GET_BASE_URL_WITH_ID(String id) {
            return BASE_URL_WITH_ID.replace("{id}", id);
        }
    }

    public static class ProductAPI {

        public static final String BASE_URL = "/products";
        private static final String BASE_URL_WITH_ID = "/products/{id}";
        public static String GET_BASE_URL_WITH_ID(String id) {
            return BASE_URL_WITH_ID.replace("{id}", id);
        }
    }

    public static class StockAPI {

        public static final String BASE_URL = "/stocks";
        public static final String BASE_URL_WITH_ID = "/stocks/{id}";
        public static String GET_BASE_URL_WITH_ID(String id) {
            return BASE_URL_WITH_ID.replace("{id}", id);
        }
    }

    public static class TransactionAPI {
        public static final String BASE_URL = "/transactions";
        public static final String DETAIL = "/transactions/detail";
        public static final String BASE_URL_WITH_ID = "/transactions/{id}";
        public static final String DETAIL_WITH_ID = "/transactions/detail/{id}";
        public static final String REPORT_URL = "/transactions/report";

        public static String GET_BASE_URL_WITH_ID(String id) {
            return BASE_URL_WITH_ID.replace("{id}", id);
        }
        public static String GET_DETAIL_WITH_ID(String id) {
            return DETAIL_WITH_ID.replace("{id}", id);
        }
    }

    public static class AdminAPI {
        public static final String BASE_URL = "/admins";
        private static final String BASE_URL_WITH_ID = "/admins/{id}";
        public static String GET_BASE_URL_WITH_ID(String id) {
            return BASE_URL_WITH_ID.replace("{id}", id);
        }
    }

    public class DashboardAPI {
        public static final String BASE_URL = "/dashboard";
    }
}
