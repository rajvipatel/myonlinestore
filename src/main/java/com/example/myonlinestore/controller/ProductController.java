package com.example.myonlinestore.controller;

import com.example.myonlinestore.model.Product;
import com.example.myonlinestore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "index";
    }
    
    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }
    
    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }

}