package au.com.equifax.cicddemo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class ApiController {

    @RequestMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();

        return "<!doctype html>"
                + "<html lang='es'>"
                + "<head>"
                + "<meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<title>Taller CI/CD - Juan Esteban Ruiz</title>"
                + "<style>"
                + "body{margin:0;font-family:Arial,sans-serif;background:#0f172a;color:#e5e7eb;}"
                + ".wrap{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:32px;}"
                + ".card{max-width:760px;background:#111827;border:1px solid #334155;border-radius:24px;padding:40px;box-shadow:0 24px 60px rgba(0,0,0,.35);}"
                + "h1{margin:0 0 12px;font-size:42px;color:#38bdf8;}"
                + "p{font-size:18px;line-height:1.6;color:#cbd5e1;}"
                + ".grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-top:28px;}"
                + ".item{background:#0b1220;border:1px solid #1e293b;border-radius:16px;padding:18px;}"
                + ".label{font-size:12px;color:#94a3b8;text-transform:uppercase;letter-spacing:.08em;}"
                + ".value{margin-top:8px;font-weight:bold;color:#f8fafc;word-break:break-word;}"
                + ".badge{display:inline-block;margin-top:24px;padding:10px 14px;border-radius:999px;background:#064e3b;color:#a7f3d0;font-weight:bold;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<main class='wrap'>"
                + "<section class='card'>"
                + "<h1>Taller CI/CD</h1>"
                + "<p><strong>Juan Esteban Ruiz</strong></p>"
                + "<p>Aplicacion desplegada con Jenkins, Docker, SonarQube y Trivy.</p>"
                + "<span class='badge'>Pipeline activo</span>"
                + "<div class='grid'>"
                + "<div class='item'><div class='label'>Host</div><div class='value'>" + inetAddress.getHostName() + "</div></div>"
                + "<div class='item'><div class='label'>IP</div><div class='value'>" + inetAddress.getHostAddress() + "</div></div>"
                + "<div class='item'><div class='label'>Sistema</div><div class='value'>" + System.getProperty("os.name") + "</div></div>"
                + "</div>"
                + "</section>"
                + "</main>"
                + "</body>"
                + "</html>";
    }
}