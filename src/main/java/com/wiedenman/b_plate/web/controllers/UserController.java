package com.wiedenman.b_plate.web.controllers;

import com.wiedenman.b_plate.model.*;
import com.wiedenman.b_plate.model.data.RoleDao;
import com.wiedenman.b_plate.model.data.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
//import org.thymeleaf.context.WebContext;
//import sun.misc.Request;


import javax.validation.Valid;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    UserDao userDao;

    @Autowired
    RoleDao roleDao;

    @RequestMapping(value = "index")
    public String index(Model model) {
        model.addAttribute("users", userDao.findAll());
        model.addAttribute("title", "Users");

        return "user/index";
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public String singleUser(Model model, @PathVariable long id) {

        User user = userDao.findOne(id);
        String formattedDate = user.getCreationDate().format(DateTimeFormatter.ofPattern("MMMM dd,  yyyy"));
        model.addAttribute("user", user);
        model.addAttribute("formattedDate", formattedDate);
        return "user/single"; // TODO: create html user/single
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)  // Displays form
    public String displayAddUser(Model model) {
        model.addAttribute("title", "Add User");
        model.addAttribute(new User());

        return "user/register";
    }

    @RequestMapping(value = "register", method = RequestMethod.POST) // Process form
    public String processAddUser(@ModelAttribute @Valid User newUser,
                                 Errors errors, Model model) {

        model.addAttribute("title", "Add User");
        String newUserEmail = newUser.getEmail();
//        .equals(userDao.findByEmail(newUser.getEmail());
        Optional<User> existingUser = userDao.findByEmail(newUserEmail);
        if (errors.hasErrors()) {
            return "user/register";

        } else if (existingUser.isPresent()) { // TODO: Add error as Error for existing user, pass to view
            return "user/register";
        }

//            newUser.setRole();  // TODO: Deal with this; move to sensible place?


        newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
        newUser.setVerifyPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
        userDao.save(newUser);
        return "redirect:index";
    }
}
