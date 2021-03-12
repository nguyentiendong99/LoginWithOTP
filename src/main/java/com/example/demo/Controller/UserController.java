package com.example.demo.Controller;

import com.example.demo.Entity.User;
import com.example.demo.Service.UserService;
import com.example.demo.Entity.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Controller
public class UserController {
    @Autowired
    private UserService service;
    @RequestMapping("/")
    public String HomePage(){
        return "homepage";
    }
    @GetMapping("/login")
    public String showLoginPage(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken){
            return "login";
        }else{
            return "redirect:/";
        }
    }
    @RequestMapping("/logout")
    public String homePage(){
        return "redirect:/login";
    }


    // TODO : create features for register verification code

    @RequestMapping(value = "/register")
    public ModelAndView ShowCustomerRegistration(Model model){
        ModelAndView modelAndView = new ModelAndView("form_register");
        model.addAttribute("user" , new User());
        return modelAndView;
    }
    @RequestMapping(value = "/createUser" , method = RequestMethod.POST)
    public String createUser(@ModelAttribute("user") User user
            , Model model , HttpServletRequest request) throws UnsupportedEncodingException, MessagingException, MessagingException {
        service.registerUser(user);
        String siteUrl = Utility.getSiteUrl(request);
        service.sendVerificationEmail(user , siteUrl);
        model.addAttribute("pageTitle" , "Register Success");
        return "register_success";
    }
    @GetMapping("/verify")
    public String verifyAccount(@Param("code") String code , Model model){
        boolean verified = service.verify(code);
        String pageTitle = verified ? "Verification Successed !" : "Verification Failed";
        model.addAttribute("pageTitle" , pageTitle);
        return (verified ? "verify_success" : "verify_fail");
    }
}
