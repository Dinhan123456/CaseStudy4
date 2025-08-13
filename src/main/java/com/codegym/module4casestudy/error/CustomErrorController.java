package com.codegym.module4casestudy.error;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            // Thêm thông tin lỗi vào model
            model.addAttribute("status", statusCode);
            model.addAttribute("message", message != null ? message.toString() : "Không có thông tin chi tiết");
            model.addAttribute("path", path != null ? path.toString() : "Không xác định");
            model.addAttribute("timestamp", new Date());
            
            // Xử lý các loại lỗi cụ thể
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "error/401";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
        }
        
        // Trả về trang lỗi chung cho các lỗi khác
        model.addAttribute("status", status != null ? status.toString() : "500");
        model.addAttribute("error", "Đã xảy ra lỗi");
        model.addAttribute("message", message != null ? message.toString() : "Không có thông tin chi tiết");
        model.addAttribute("path", path != null ? path.toString() : "Không xác định");
        
        return "error/error";
    }

    public String getErrorPath() {
        return "/error";
    }
}
