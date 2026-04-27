package com.tunesharehub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

class AuthUserArgumentResolverTest {

    private final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

    @Test
    void optionalCurrentUserReturnsNullWhenRequestHasNoAuthUser() throws Exception {
        MethodParameter parameter = getParameter("optionalCurrentUser");
        NativeWebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

        Object resolved = resolver.resolveArgument(parameter, null, webRequest, null);

        assertThat(resolved).isNull();
    }

    @Test
    void requiredCurrentUserRejectsMissingAuthUser() throws Exception {
        MethodParameter parameter = getParameter("requiredCurrentUser");
        NativeWebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

        assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("인증 사용자 정보가 필요합니다.");
    }

    @SuppressWarnings("unused")
    void optionalCurrentUser(@CurrentUser(required = false) AuthUser authUser) {
    }

    @SuppressWarnings("unused")
    void requiredCurrentUser(@CurrentUser AuthUser authUser) {
    }

    private MethodParameter getParameter(String methodName) throws NoSuchMethodException {
        Method method = AuthUserArgumentResolverTest.class.getDeclaredMethod(methodName, AuthUser.class);
        return new MethodParameter(method, 0);
    }
}
