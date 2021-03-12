package com.example.demo.Service;

import com.example.demo.Entity.User;
import com.example.demo.Handler.UsernameNotFoundException;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Date;

@Transactional
@Service
public class UserService {
    public static final int MAX_FAILED_ATTEMPT = 3;
    private static final long LOCK_TIME_DURATION = 10 * 60 * 1000; // 10 minutes
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender;

// TODO : Register and Verification Email
    public void registerUser(User user){
        user.setEnabled(false);
        String randomCode = RandomString.make(64);
        user.setVerificationCode(randomCode);
        userRepository.save(user);
    }

    public void sendVerificationEmail(User user , String siteUrl) throws UnsupportedEncodingException, MessagingException, MessagingException {
        String subject = "Please verify your register";
        String senderName = "Shop Mobile";
        String mailContent = "<p> Dear " + user.getUsername() + ", </p>";
        mailContent += "<p> Please click the link below to verify to your registration </p>";
        String verifyUrl = siteUrl + "/verify?code=" + user.getVerificationCode();
        mailContent += "<h3><a href=\"" + verifyUrl + "\">VERIFY</a></h3>";
        mailContent += "<p>Thank you <br> The Mobile Shop</p>";
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("dong19069999@gmail.com" , senderName);
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(mailContent , true);
        javaMailSender.send(message);
    }
    public boolean verify(String verificationCode){
        User user = userRepository.findByVeritification(verificationCode);
        if (user == null || user.isEnabled()){
            return false;
        }
        else {
            userRepository.enable(user.getId());
            return true;
        }
    }


    // TODO : Limit Login

    public void increaseFailedAttempt(User user){
        int newFailedAttempts  = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempt(newFailedAttempts , user.getUsername());
    }
    // lock account
    public void lock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }
    //unlock account
    public boolean unclock(User user){
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();
        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis){
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void resetFailedAttempt(String username) {
        userRepository.updateFailedAttempt(0 , username);
    }

    // TODO: Update reset password
    public void updateResetPasswordToken(String token , String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user != null){
            user.setResetPasswordToken(token);
            userRepository.save(user);
        }
        else {
            throw new UsernameNotFoundException("Could not find any user with email " + email);
        }
    }
    public User get(String resetPasswordToken){
        return userRepository.findByResetPasswordToken(resetPasswordToken);
    }
    public void updatePassword(User user , String newPassword){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodePassword);
        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

    public void generateOneTimePassword(User user)
            throws UnsupportedEncodingException, MessagingException {
        String OTP = RandomString.make(8);
        System.out.println("OTP : " + OTP);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodeOTP = passwordEncoder.encode(OTP);
        user.setOneTimePassword(encodeOTP);
        user.setOtpRequestedTime(new Date());
        userRepository.save(user);
        sendOTPEmail(user , OTP);
    }

    private void sendOTPEmail(User user, String otp) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("dongnguyen@gmail.com" , "dongnguyen");
        helper.setTo(user.getEmail());
        String subject = "Here's your One Time Password (OTP) - Expired after 5 minutes ";
        String content = "<p>Hello " + user.getUsername() + "</p>"
                + "<p> For secure reason , you're required to the follow OTP login : </p>"
                + "<p><b>" + otp + "</b></p>";
        helper.setSubject(subject);
        helper.setText(content , true);
        javaMailSender.send(message);

    }

    public void clearOTP(User user) {
        user.setOneTimePassword(null);
        user.setOtpRequestedTime(null);
        userRepository.save(user);
        System.out.println("Cleared OTP");
    }
}
