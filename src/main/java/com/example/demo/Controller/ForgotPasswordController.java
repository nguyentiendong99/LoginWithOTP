package com.example.demo.Controller;

import com.example.demo.Entity.User;
import com.example.demo.Service.UserService;
import com.example.demo.Handler.UsernameNotFoundException;
import com.example.demo.Entity.Utility;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Controller
public class ForgotPasswordController {
    @Autowired
    private UserService service;
    @Autowired
    private JavaMailSender javaMailSender;

    @GetMapping("/forgot_password")
    public String showForgotPasswordForm(Model model){
        model.addAttribute("pageTitle" , "Forgot Password");
        return "forgot_password_form";
    }
    @PostMapping("/forgot_password")
    public String processForgotPasswordForm(HttpServletRequest request,
                                            Model model){
        String email = request.getParameter("email");
        String token = RandomString.make(45);
        try{
            service.updateResetPasswordToken(token , email);
            String resetPasswordLink = Utility.getSiteUrl(request) + "/reset_password?token=" + token;
            sendEmail(email , resetPasswordLink);
            model.addAttribute("message" , "we have sent a reset password , please check email ");
        }catch (UsernameNotFoundException exception){
            model.addAttribute("error" , exception.getMessage());
        } catch (UnsupportedEncodingException e) {
            model.addAttribute("error" , "Error while sending email");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return "forgot_password_form";
    }

    private void sendEmail(String email, String resetPasswordLink) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("contact@dongnguyen.com" , "dongnguyen");
        helper.setTo(email);
        String subject = "Here's the link to reset your password";
        String content = "<p>Hello, </p>"
                + "<p>you have requested to reset password</p>"
                + " <p>click the link below to reset password</p>"
                + "<p><b><a href=\""+ resetPasswordLink + "\">change my password </a></b></p>";
        helper.setSubject(subject);
        helper.setText(content ,true);
        javaMailSender.send(message);
    }
    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param("token") String token
                                        , Model model){
        User user = service.get(token);
        if (user == null){
            model.addAttribute("message", "Invalid Token");
            return "message";
        }
        model.addAttribute("token" , token);
        return "reset_password_form";
    }
    @PostMapping("/reset_password")
    public String processResetPassword(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");

        User user = service.get(token);
        model.addAttribute("title", "Reset your password");

        if (user == null) {
            model.addAttribute("message", "Invalid Token");
            return "message";
        } else {
            service.updatePassword(user, password);

            model.addAttribute("message", "You have successfully changed your password.");
        }

        return "message";
    }
}
