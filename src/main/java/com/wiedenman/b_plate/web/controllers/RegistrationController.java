package com.wiedenman.b_plate.web.controllers;

import com.wiedenman.b_plate.exception.EmailExistsException;
import com.wiedenman.b_plate.service.EmailService;
import com.wiedenman.b_plate.service.UserService;
import com.wiedenman.b_plate.web.model.User;
import com.wiedenman.b_plate.web.model.VerificationToken;
import com.wiedenman.b_plate.web.model.data.UserDao;
import com.wiedenman.b_plate.web.model.data.VerificationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.UUID;

/**
 *    888                      888          888
 *    888                      888          888
 *    888                      888          888
 *    88888b.         88888b.  888  8888b.  888888 .d88b.
 *    888 "88b        888 "88b 888     "88b 888   d8P  Y8b
 *    888  888 888888 888  888 888 .d888888 888   88888888
 *    888 d88P        888 d88P 888 888  888 Y88b. Y8b.
 *    88888P"         88888P"  888 "Y888888  "Y888 "Y8888
 *                    888
 *                    888
 *                    888
 *
 *
 * @author Landon Wiedenman
 * github.com/landongw/b-plate
 * Usage: or personal non-commercial use only.  Please contact me for commercial uses.
 *
 * Copyright (c) 2017 Landon Wiedenman
 */

@Controller
public class RegistrationController {

    @Autowired
    UserDao userDao;

    @Autowired
    UserService userService;

    @Autowired
    VerificationDao verificationDao;

    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "register", method = RequestMethod.GET)  // Displays form
    public String registrationForm(Model model) {
        model.addAttribute("title", "Register");
        model.addAttribute(new User());

        return "registrationPage";
        }

    @RequestMapping(value = "register", method = RequestMethod.POST) // Process form
    public String registerUser(@ModelAttribute @Valid User newUser,
                               final BindingResult result,
                               Errors errors, Model model,
                               final HttpServletRequest request) {

        model.addAttribute("title", "Register");

        if (errors.hasErrors()) {
            return "registrationPage";
        }
        try {
            newUser.setEnabled(false);
            userService.registerNewUser(newUser);

            final String token = UUID.randomUUID().toString();
            final VerificationToken vToken = new VerificationToken(token, newUser);
            verificationDao.save(vToken);

            final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();  // TODO: update this to https in production

            // Email message
            SimpleMailMessage passwordResetEmail = new SimpleMailMessage();
            passwordResetEmail.setFrom("donotreply@b-plate.com");  // TODO: change email message for production
            passwordResetEmail.setTo(newUser.getEmail());
            passwordResetEmail.setSubject("Verify your b-plate account");
            passwordResetEmail.setText("To verify your account click the link and login:\n" + appUrl
                    + "/verify?verification_token=" + vToken.getToken());
            emailService.sendEmail(passwordResetEmail);
            // TODO: Add success message to view
//            modelAndView.addObject("successMessage", "A password reset link has been sent to " + userEmail);
        } catch (EmailExistsException e) {
            result.addError(new FieldError("user", "email", e.getMessage()));
            model.addAttribute("user", newUser);
            return "registrationPage";
        }
        return "redirect:/login";
    }

    @RequestMapping(value = "/verify")
    public ModelAndView confirmRegistration(final Model model, @RequestParam("token") final String token, final RedirectAttributes redirectAttributes) {
        final VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid account confirmation token.");
            return new ModelAndView("redirect:/login");
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your registration token has expired. Please register again.");
            return new ModelAndView("redirect:/login");
        }

        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        redirectAttributes.addFlashAttribute("message", "Your account has been verified! Sick!");
        return new ModelAndView("redirect:/login");
    }
}
