package com.example.demo.Handler;

import com.example.demo.Entity.User;
import com.example.demo.Service.UserRepository;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@Component
public class BeforeAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService service;

    public BeforeAuthenticationFilter() {
        super.setUsernameParameter("email");
        super.setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String email = request.getParameter("email");
        System.out.println("attemptAuthentication email : " + email);
        User user = repository.findByEmail(email);
        int condition = user.getFailedAttempt();
        if (user != null) {
            float spamCore = getGoogleRecaptchaScore();
            if (spamCore < 0.2) {
                if (user.isOTPRequired()) {
                    return super.attemptAuthentication(request, response);
                }
                try {
                    service.generateOneTimePassword(user);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                throw new InsufficientAuthenticationException("OTP");
            }
        }
        return super.attemptAuthentication(request, response);
    }

    //TODO : điều kiện để xét xem login bằng OTP hay normal password
    private float getGoogleRecaptchaScore() {
        return 0.43f;
    }

    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Autowired
    @Override
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        super.setAuthenticationSuccessHandler(successHandler);
    }

    @Autowired
    @Override
    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        super.setAuthenticationFailureHandler(failureHandler);
    }
}
