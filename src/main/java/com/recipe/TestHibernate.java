package com.recipe;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestHibernate {
    public static void main(String[] args) {
        // Open session
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        // Create a new recipe
        Recipe recipe = new Recipe("Mango Juice", "Juice", "Mango Extract, Sugar, Water");

        // Save it
        session.persist(recipe);

        tx.commit();
        session.close();

        System.out.println("Recipe saved successfully!");
    }
}
