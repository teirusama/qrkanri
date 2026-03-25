package org.qrkanri.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.Employee;
import org.qrkanri.entity.QrToken;
import org.qrkanri.service.AttendanceService;
import org.qrkanri.service.QrTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class QrController {
    private final QrTokenService qrTokenService;
    private final AttendanceService attendanceService;

    @GetMapping("/qr/image")
    public void generateQrImage(HttpServletResponse response) throws Exception{
        QrToken token = qrTokenService.generateToken();
        String url = "/qr/check?token=" + token.getToken();

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 300, 300);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        response.setContentType("image/png");
        ImageIO.write(qrImage, "PNG", response.getOutputStream());
    }

    @GetMapping("/qr/display")
    public String qrDisplay(){
        return "qr-display";
    }

    @GetMapping("/qr/checkin")
    @ResponseBody
    public Map<String,Object> checkin(@RequestParam String token,
                                      @RequestParam(required = false, defaultValue = "false") boolean force,
                                      HttpSession session){
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null){
            throw new RuntimeException("ログインが必要です。");
        }
        String result = attendanceService.processQr(employee, token, force);
        Map<String,Object> response = new HashMap<>();
        response.put("result", result);
        System.out.println("QR RESULT: " + result);
        return response;
    }

    @GetMapping("/qr-scan")
    public String qrScanPage(){
        return "qr-scan";
    }
}
