package com.immobilier.rest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import com.google.gson.Gson;

// ✅ CORRECTION: Changer le mapping
@WebServlet("/api/users/*")  // Au lieu de /users/*
public class UtilisateursServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final UserDAO dao = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json;charset=UTF-8");

        // Ajouter CORS headers
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        try {
            if("/login".equalsIgnoreCase(path)){
                Utilisateur in = gson.fromJson(req.getReader(), Utilisateur.class);
                Utilisateur found = dao.findByEmailAndPassword(in.getEmail(), in.getPassword());

                if(found != null) {
                    found.setPassword(null); // ✅ Ne pas renvoyer le mot de passe
                    resp.setStatus(200);
                    resp.getWriter().write(gson.toJson(found));
                } else {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"invalid credentials\"}");
                }
                return;
            }

            if("/register".equalsIgnoreCase(path)){
                Utilisateur in = gson.fromJson(req.getReader(), Utilisateur.class);

                // Validation
                if(in.getEmail() == null || in.getPassword() == null || in.getNom() == null) {
                    resp.setStatus(400);
                    resp.getWriter().write("{\"error\":\"missing fields\"}");
                    return;
                }

                if(dao.findByEmail(in.getEmail()) != null) {
                    resp.setStatus(409);
                    resp.getWriter().write("{\"error\":\"email already exists\"}");
                    return;
                }

                Utilisateur created = dao.create(in);
                if(created != null) {
                    resp.setStatus(201);
                    resp.getWriter().write(gson.toJson(created));
                } else {
                    resp.setStatus(500);
                    resp.getWriter().write("{\"error\":\"creation failed\"}");
                }
                return;
            }

            resp.setStatus(404);
            resp.getWriter().write("{\"error\":\"endpoint not found\"}");

        } catch(Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ✅ AJOUTER: Support OPTIONS pour CORS
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(200);
    }
}