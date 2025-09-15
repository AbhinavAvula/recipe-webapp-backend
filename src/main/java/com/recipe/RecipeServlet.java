package com.recipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/recipes/*")
public class RecipeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            String pathInfo = request.getPathInfo(); // e.g. "/1"
            ObjectMapper mapper = new ObjectMapper();

            if (pathInfo == null || pathInfo.equals("/")) {
                // CASE 1: Return all recipes
                Query<Recipe> query = session.createQuery("from Recipe", Recipe.class);
                List<Recipe> recipes = query.list();
                tx.commit();

                String json = mapper.writeValueAsString(recipes);
                response.getWriter().write(json);
            } else {
                // CASE 2: Return recipe by ID
                try {
                    int id = Integer.parseInt(pathInfo.substring(1)); // remove "/"
                    Recipe recipe = session.get(Recipe.class, id);
                    tx.commit();

                    if (recipe != null) {
                        String json = mapper.writeValueAsString(recipe);
                        response.getWriter().write(json);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\":\"Recipe not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid recipe ID\"}");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        try (BufferedReader reader = request.getReader();
             Session session = HibernateUtil.getSessionFactory().openSession()) {

            // Convert JSON from request into Recipe object
            ObjectMapper mapper = new ObjectMapper();
            Recipe newRecipe = mapper.readValue(reader, Recipe.class);

            Transaction tx = session.beginTransaction();
            session.save(newRecipe);
            tx.commit();

            // Respond with success message and generated ID
            String jsonResponse = String.format(
                "{\"message\":\"Recipe created successfully\", \"id\":%d}", newRecipe.getId()
            );
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
