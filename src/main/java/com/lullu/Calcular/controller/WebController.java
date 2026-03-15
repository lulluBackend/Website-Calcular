package com.lullu.Calcular.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;

import com.lullu.Calcular.model.userModel;
import com.lullu.Calcular.service.userService;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private userService userservice;

    // ========== PÁGINAS PRINCIPAIS ==========
    
    // Home - página principal
    @GetMapping("/")
    public ModelAndView home(HttpSession session) {
        ModelAndView mv = new ModelAndView("home");
        
        // Verifica se tem usuário logado
        String nome = (String) session.getAttribute("userName");
        
        if (nome != null) {
            mv.addObject("userName", nome);
            mv.addObject("userStatus", "Logado como: " + nome);
        } else {
            mv.addObject("userName", "Anônimo");
            mv.addObject("userStatus", "Você está navegando como anônimo");
        }
        
        return mv;
    }

    // Página de registro
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // Página de login
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Processar registro
    @PostMapping("/register")
    public String register(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            HttpSession session) {

        Optional<userModel> usuarioExistente = userservice.findByEmail(email);
        if (usuarioExistente.isPresent()) {
            return "redirect:/register?error=email_exists";
        }

        userModel user = new userModel();
        user.setNome(nome);
        user.setEmail(email);
        user.setSenha(senha);
        
        userservice.save(user);
        
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getNome());
        session.setAttribute("userEmail", user.getEmail());
        
        return "redirect:/";
    }

    // Processar login
    @PostMapping("/login")
    public ModelAndView login(
            @RequestParam String email,
            @RequestParam String senha,
            HttpSession session) {
        
        ModelAndView mv = new ModelAndView();
        
        Optional<userModel> usuarioOpt = userservice.findByEmail(email);
        
        if (usuarioOpt.isPresent()) {
            userModel usuario = usuarioOpt.get();
            
            if (usuario.getSenha().equals(senha)) {
                session.setAttribute("userId", usuario.getId());
                session.setAttribute("userName", usuario.getNome());
                session.setAttribute("userEmail", usuario.getEmail());
                
                mv.setViewName("redirect:/");
                return mv;
            } else {
                mv.setViewName("login");
                mv.addObject("error", "Senha incorreta!");
                mv.addObject("email", email);
                return mv;
            }
        } else {
            mv.setViewName("login");
            mv.addObject("error", "Email não cadastrado!");
            return mv;
        }
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }


    // Página de cálculo de feriados
    @GetMapping("/feriados")
    public ModelAndView feriadosPage(HttpSession session) {
        ModelAndView mv = new ModelAndView("feriados");
        addUserInfoToModel(mv, session);
        return mv;
    }

    @PostMapping("/calcular-feriado")
    public ModelAndView calcularFeriado(
            @RequestParam String feriado,
            @RequestParam(required = false) Integer dia,
            @RequestParam(required = false) Integer mes,
            HttpSession session) {
        
        ModelAndView mv = new ModelAndView("feriados");
        addUserInfoToModel(mv, session);
        
        LocalDate hoje = LocalDate.now();
        LocalDate dataFeriado = null;
        String nomeFeriado = "";
        
        try {
            if ("personalizado".equals(feriado) && dia != null && mes != null) {
                dataFeriado = LocalDate.of(hoje.getYear(), mes, dia);
                nomeFeriado = "Data personalizada: " + dia + "/" + mes;
            } else {
                switch (feriado) {
                    case "natal":
                        dataFeriado = LocalDate.of(hoje.getYear(), Month.DECEMBER, 25);
                        nomeFeriado = "Natal";
                        break;
                    case "pascoa":
                    
                        dataFeriado = calcularPascoa(hoje.getYear());
                        nomeFeriado = "Páscoa";
                        break;
                    case "anoNovo":
                        dataFeriado = LocalDate.of(hoje.getYear() + 1, Month.JANUARY, 1);
                        nomeFeriado = "Ano Novo";
                        break;
                    case "carnaval":
                        dataFeriado = calcularCarnaval(hoje.getYear());
                        nomeFeriado = "Carnaval";
                        break;
                    case "tiradentes":
                        dataFeriado = LocalDate.of(hoje.getYear(), Month.APRIL, 21);
                        nomeFeriado = "Tiradentes";
                        break;
                    case "independencia":
                        dataFeriado = LocalDate.of(hoje.getYear(), Month.SEPTEMBER, 7);
                        nomeFeriado = "Independência do Brasil";
                        break;
                    case "aparecida":
                        dataFeriado = LocalDate.of(hoje.getYear(), Month.OCTOBER, 12);
                        nomeFeriado = "Nossa Srª Aparecida";
                        break;
                    case "finados":
                        dataFeriado = LocalDate.of(hoje.getYear(), Month.NOVEMBER, 2);
                        nomeFeriado = "Finados";
                        break;
                    case "proclamacao":
                        dataFeriado = LocalDate.of(hoje.getYear(), Month.NOVEMBER, 15);
                        nomeFeriado = "Proclamação da República";
                        break;
                    default:
                        mv.addObject("erro", "Feriado não reconhecido");
                        return mv;
                }
            }
            
            long diasRestantes = ChronoUnit.DAYS.between(hoje, dataFeriado);
            
            if (diasRestantes < 0) {
                // Se já passou, calcular para o próximo ano
                dataFeriado = dataFeriado.plusYears(1);
                diasRestantes = ChronoUnit.DAYS.between(hoje, dataFeriado);
                mv.addObject("aviso", "O feriado deste ano já passou! Mostrando para o próximo ano.");
            }
            
            mv.addObject("resultado", String.format("Faltam %d dias para %s!", diasRestantes, nomeFeriado));
            mv.addObject("dataFeriado", dataFeriado.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
        } catch (Exception e) {
            mv.addObject("erro", "Erro ao calcular a data. Verifique os valores informados.");
        }
        
        return mv;
    }

    // Página de Bhaskara
    @GetMapping("/bhaskara")
    public ModelAndView bhaskaraPage(HttpSession session) {
        ModelAndView mv = new ModelAndView("bhaskara");
        addUserInfoToModel(mv, session);
        return mv;
    }

    @PostMapping("/calcular-bhaskara")
    public ModelAndView calcularBhaskara(
            @RequestParam double a,
            @RequestParam double b,
            @RequestParam double c,
            HttpSession session) {
        
        ModelAndView mv = new ModelAndView("bhaskara");
        addUserInfoToModel(mv, session);
        
        double delta = b * b - 4 * a * c;
        
        mv.addObject("a", a);
        mv.addObject("b", b);
        mv.addObject("c", c);
        mv.addObject("delta", delta);
        
        if (delta < 0) {
            mv.addObject("resultado", "Não existem raízes reais (Δ < 0)");
        } else if (delta == 0) {
            double x = -b / (2 * a);
            mv.addObject("resultado", String.format("Δ = 0 → Uma raiz real: x = %.2f", x));
            mv.addObject("x1", x);
        } else {
            double x1 = (-b + Math.sqrt(delta)) / (2 * a);
            double x2 = (-b - Math.sqrt(delta)) / (2 * a);
            mv.addObject("resultado", String.format("Δ = %.2f → Duas raízes reais: x' = %.2f e x'' = %.2f", delta, x1, x2));
            mv.addObject("x1", x1);
            mv.addObject("x2", x2);
        }
        
        return mv;
    }

    // Página de Regra de Três
    @GetMapping("/regra-tres")
    public ModelAndView regraTresPage(HttpSession session) {
        ModelAndView mv = new ModelAndView("regra-tres");
        addUserInfoToModel(mv, session);
        return mv;
    }

    @PostMapping("/calcular-regra-tres")
    public ModelAndView calcularRegraTres(
            @RequestParam double valor1,
            @RequestParam double valor2,
            @RequestParam double valor3,
            HttpSession session) {
        
        ModelAndView mv = new ModelAndView("regra-tres");
        addUserInfoToModel(mv, session);
        
        
        double x = (valor2 * valor3) / valor1;
        
        mv.addObject("valor1", valor1);
        mv.addObject("valor2", valor2);
        mv.addObject("valor3", valor3);
        mv.addObject("resultado", String.format("%.2f / %.2f = %.2f / x  →  x = %.2f", valor1, valor2, valor3, x));
        mv.addObject("x", x);
        
        return mv;
    }

    // Página de Fatorial
    @GetMapping("/fatorial")
    public ModelAndView fatorialPage(HttpSession session) {
        ModelAndView mv = new ModelAndView("fatorial");
        addUserInfoToModel(mv, session);
        return mv;
    }

    @PostMapping("/calcular-fatorial")
    public ModelAndView calcularFatorial(
            @RequestParam int numero,
            HttpSession session) {
        
        ModelAndView mv = new ModelAndView("fatorial");
        addUserInfoToModel(mv, session);
        
        if (numero < 0) {
            mv.addObject("erro", "Fatorial de número negativo não existe!");
            return mv;
        }
        
        long fatorial = 1;
        StringBuilder calculo = new StringBuilder();
        
        for (int i = 1; i <= numero; i++) {
            fatorial *= i;
            if (i > 1) calculo.append(" × ");
            calculo.append(i);
        }
        
        mv.addObject("numero", numero);
        mv.addObject("fatorial", fatorial);
        mv.addObject("calculo", numero + "! = " + calculo.toString() + " = " + fatorial);
        
        return mv;
    }
    
    private void addUserInfoToModel(ModelAndView mv, HttpSession session) {
        String nome = (String) session.getAttribute("userName");
        if (nome != null) {
            mv.addObject("userName", nome);
            mv.addObject("userStatus", "Logado como: " + nome);
        } else {
            mv.addObject("userName", "Anônimo");
            mv.addObject("userStatus", "Visitante");
        }
    }

    private LocalDate calcularPascoa(int ano) {
        // Algoritmo de Gauss para calcular a Páscoa
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        
        return LocalDate.of(ano, mes, dia);
    }

    private LocalDate calcularCarnaval(int ano) {
    
        return calcularPascoa(ano).minusDays(47);
    }


// Página de Delta
@GetMapping("/delta")
public ModelAndView deltaPage(HttpSession session) {
    ModelAndView mv = new ModelAndView("delta");
    addUserInfoToModel(mv, session);
    return mv;
}

@PostMapping("/calcular-delta")
public ModelAndView calcularDelta(
        @RequestParam double a,
        @RequestParam double b,
        @RequestParam double c,
        HttpSession session) {
    
    ModelAndView mv = new ModelAndView("delta");
    addUserInfoToModel(mv, session);
    
    double delta = b * b - 4 * a * c;
    
    mv.addObject("a", a);
    mv.addObject("b", b);
    mv.addObject("c", c);
    mv.addObject("delta", delta);
    
    if (delta < 0) {
        mv.addObject("resultado", "Δ = " + delta + " → Não existem raízes reais");
        mv.addObject("classificacao", "Delta negativo");
    } else if (delta == 0) {
        mv.addObject("resultado", "Δ = " + delta + " → Duas raízes iguais (x₁ = x₂)");
        mv.addObject("classificacao", "Delta igual a zero");
    } else {
        mv.addObject("resultado", "Δ = " + delta + " → Duas raízes reais e diferentes");
        mv.addObject("classificacao", "Delta positivo");
    }
    
    return mv;
}


}