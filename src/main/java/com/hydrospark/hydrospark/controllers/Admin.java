package com.hydrospark.hydrospark.controllers;


import com.hydrospark.hydrospark.entities.Employee;
import com.hydrospark.hydrospark.entities.Product;
import com.hydrospark.hydrospark.entities.SubProducts;
import com.hydrospark.hydrospark.repositories.EmployeeRepo;
import com.hydrospark.hydrospark.repositories.ProductRepo;
import com.hydrospark.hydrospark.repositories.SubProdRepo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.Part;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

import static ch.qos.logback.core.util.StringUtil.capitalizeFirstLetter;

@Controller
@RequestMapping("admin")
public class Admin {

    @Autowired
    private EmployeeRepo employeeRepo;


    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private SubProdRepo subProdRepo;

    @GetMapping("")
    public String adminHome(){
        System.out.println("Here");
        return "admin.html";
    }



    @GetMapping("/employeeLogin")
    public String getEmployeeLogin(HttpSession session){
        session.setAttribute("error","In employeeLogin get");
        return "employeeSignin.html";
    }
    @PostMapping("/employeeLogin")
    public String PostEmployeeLogin(HttpServletRequest request, Model model, HttpSession session){
        System.out.println(session.getAttribute("error"));
        session.setAttribute("error","In employeeLogin post");
        String email=request.getParameter("Email");
        String password=request.getParameter("Password");
        List<Employee> employees=employeeRepo.findEmployeeByEmailAndPassword(email,password);
        System.out.println("Employees: "+employees);
        if(employees.size()>0){
            session.setAttribute("employee",employees.get(0).getEmail());
            session.setAttribute("role",employees.get(0).getRole());
            System.out.println("Hereeeeeeeeeeeeeee");

        }
        else{
            session.setAttribute("error","email or password is wrong");
            return "employeeSignin.html";
        }
        return "redirect:/admin/";
    }


    @GetMapping("/addEmployee")
    public String getAddEmployee(Model model){
        System.out.println(model.getAttribute("employee"));
        return "addEmployee.html";
    }
    public int findLastDigit(String email){
        List<Character> numbers=List.of('1','2','3','4','5','6','7','8','9','0');
        String curr="";
        email=email.split("@")[0];
        System.out.println(email);
        for(int i=email.length()-1;i>=0;i--){
            if (numbers.contains(email.charAt(i))){
                curr+=email.charAt(i);
            }
        }
        System.out.println(curr);
        return Integer.parseInt(curr);
    }

    @PostMapping("/addEmployee")
    public String postAddEmployee(HttpServletRequest request){
        String firstName=request.getParameter("firstName");
        String lastName=request.getParameter("lastName");
        String role=request.getParameter("employeeRoles");
        List<Employee> employees=employeeRepo.findEmployeeByFullname(firstName,lastName);
        int mx=0;
        if (employees.size()>0){
            for(Employee emp:employees){
                mx=Math.max(mx,findLastDigit(emp.getEmail()));
            }
        }
        mx++;
        String email=firstName+lastName+mx+"@hydrospark.org";
        String password=capitalizeFirstLetter(firstName)+lastName+"@"+mx;
        Employee employee =new Employee(firstName,lastName,password,email,role);
        employeeRepo.save(employee);
        return "addEmployee.html";
    }


    @GetMapping("/addProduct")
    public String getAddProducts(HttpSession session){
        List<String> validRoles=List.of("admin","manager");
        if(session.getAttribute("employee")!=null && validRoles.contains(session.getAttribute("role"))){
            return "addProducts.html";
        }
        return "redirect:/admin/error";

    }

    @PostMapping("/addProduct")
    public String postAddProducts(HttpServletRequest request,HttpSession session) throws ServletException, IOException {
        List<String> validRoles=List.of("admin","manager");
        if(session.getAttribute("employee")!=null && validRoles.contains(session.getAttribute("role"))) {

            String prodName=request.getParameter("ProductName");
            String subtype=request.getParameter("Category");
            String description=request.getParameter("Description");
            double price= Double.parseDouble(request.getParameter("price"));

            Part filePart = request.getPart("ProductImage");
            byte[] imageBytes = filePart.getInputStream().readAllBytes();
            Product getProd=productRepo.findByName(prodName);
            if (getProd==null){
                Product product=new Product(prodName,imageBytes);
                productRepo.save(product);
//               System.out.println(this.getFileName(filePart));
            }
            getProd=productRepo.findByName(prodName);
            SubProducts subProd=new SubProducts(subtype, price, description, getProd,imageBytes);
            subProdRepo.save(subProd);

            return "redirect:/admin/";
        }
        else{
            return "redirect:/admin/error";
        }

    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String cd : contentDisposition.split(";")) {
            if (cd.trim().startsWith("filename")) {
                // Extract the file name (remove leading and trailing spaces and quotes)
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName;
            }
        }
        return null; // Return null if no filename found
    }

    @GetMapping("/prod/{id}")
    public String getProduct(@PathVariable int id,Model model) throws SQLException {
        Product product = productRepo.findById(id);
        Blob blob = new SerialBlob(product.getProdImg());
        byte[] bytes = blob.getBytes(1, (int) blob.length());
        String base64Image = Base64.getEncoder().encodeToString(bytes);
        model.addAttribute("base64Image", base64Image);
        if (product == null) {
            model.addAttribute("message", "Product not found");
            return "error";
        }

        if (product.getProdImg() != null) {
            model.addAttribute("image", product.getProdImg());
        }

        model.addAttribute("product", product);
        return "img.html";
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(@PathVariable int id) {
        Product product = productRepo.findById(id);

        if (product == null || product.getProdImg() == null) {
            return ResponseEntity.notFound().build(); // Return 404 if product or image is not found
        }

        // Set the appropriate media type (here assuming it's a JPEG, but you can change it based on the image format)
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Change this depending on your image format (e.g., IMAGE_PNG for PNG files)
                .body(product.getProdImg()); // Return the image as a byte array
    }

    @GetMapping("/removeEmployee")
    public String getRemoveEmployee(HttpSession session){
        List<String> validRoles=List.of("admin","manager");
        if(session.getAttribute("employee")!=null && validRoles.contains(session.getAttribute("role"))){
            return "removeEmployee.html";
        }
        return "redirect:/admin/error";
    }

    @PostMapping("/removeEmployee")
    public String postRemoveEmployee(HttpServletRequest request,HttpSession session,Model model){
        List<String> validRoles=List.of("admin","manager");
        if(session.getAttribute("employee")!=null && validRoles.contains(session.getAttribute("role"))){
            String empEmail=request.getParameter("Email");
            employeeRepo.deleEmployeeByEmail(empEmail);
            return "redirect:/admin/";
        }
        return "redirect:/admin/error";
    }



    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return  "redirect:/admin/";
    }
    @GetMapping("/error")
    public String error(){
        return "unauthorized.html";
    }

}
