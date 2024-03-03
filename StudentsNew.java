package st2245;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.*;
import java.sql.*;


@WebServlet("/studentsnew")
public class StudentsNew extends HttpServlet {

    private String driver = "org.sqlite.JDBC";
    private String dbURL = "jdbc:sqlite:C:/TED/workspace/eclipse/2022/st2245/src/main/webapp/WEB-INF/teddb.db";

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String operation = req.getParameter("operation");

        if ("edit".equals(operation)) {
            // If the operation is Edit, display the edit form
            displayEditForm(req, res);
            return;
        }

        Connection dbCon;

        res.setCharacterEncoding("utf-8");
        res.setContentType("text/html; charset=utf-8");

        PrintWriter out = res.getWriter();

        try {
            Class.forName(driver);
            dbCon = DriverManager.getConnection(dbURL);
            ResultSet rs;
            Statement stmt;
            stmt = dbCon.createStatement();
            rs = stmt.executeQuery("SELECT id, first_name, last_name, semester, email FROM students");

            out.println("<!DOCTYPE html><html><body>");

            printForm(out);
            printAnyError(out, req);

            // Printing the table
            out.println("<hr/>");
            out.println("<table border=1><tr>");

            String[] columns = new String[]{"id", "first_name", "last_name", "semester", "email"};
            String[] columnsVisible = new String[]{"ΑΜ", "ΟΝΟΜΑ", "ΕΠΩΝΥΜΟ", "ΕΞΑΜΗΝΟ", "EMAIL"};

            for (int i = 0; i < columns.length; i++) {
                out.print("<td><b>");
                out.print(columnsVisible[i].toUpperCase());
                out.print("</b></td>");
            }

            while (rs.next()) {
                out.println("<tr>");
                for (int i = 0; i < columns.length; i++) {
                    out.println("<td>");
                    out.println(rs.getString(columns[i]));
                    out.println("</td>");
                }

                // Add Edit link/button
                
                out.println("<td><a href=\"studentsnew?operation=edit&id=" + rs.getString("id") + "\"><input type=\"submit\" value=\"Edit\"></a></td>");

                // Add Delete form
                out.println("<td>");
                out.println("<form action=\"studentsnew\" method=\"POST\">");
                out.println("<input type=\"submit\" name=\"action\" value=\"Delete\" />");
                out.println("<input type=\"hidden\" name=\"am\" value=\"" + rs.getString("id") + "\">");
                out.println("</form>");
                out.println("</td>");

                out.println("</tr>\n");
            }
            out.println("</table></body></html>");

            rs.close();
            stmt.close();
            dbCon.close();

        } catch (Exception e) {
            out.println(e.toString());
        } finally {
            out.close();
        }
    }

    private void displayEditForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String idToEdit = req.getParameter("id");
        String query = "SELECT id, first_name, last_name, semester, email FROM students WHERE id = ?";

        try (Connection dbCon = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = dbCon.prepareStatement(query)) {

            stmt.setString(1, idToEdit);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Display the form with pre-filled fields
            	res.setCharacterEncoding("utf-8");
            	res.setContentType("text/html; charset=utf-8");
                PrintWriter out = res.getWriter();

                out.println("<!DOCTYPE html><html><body>");
                printAnyError(out, req);

                out.println("<hr/>");
                out.println("<form action=\"studentsnew\" method=\"POST\">");
                out.println("<b> Παρακαλώ τροποποιήστε τα ακόλουθα στοιχεία: </b> <br>");
                out.println("<b> Όνομα :  </b> <input type=\"text\" name=\"onoma\" value=\"" + rs.getString("first_name") + "\"><br>");
                out.println("<b> Επώνυμο :  </b> <input type=\"text\" name=\"eponimo\" value=\"" + rs.getString("last_name") + "\"><br>");
                out.println("<b> Αριθμός Μητρώου: </b> <input type=\"text\" name=\"am\" value=\"" + rs.getString("id") + "\" readonly><br>");
                out.println("<b> Εξάμηνο: </b>  <input type=\"text\" name=\"examino\" value=\"" + rs.getString("semester") + "\"><br>");
                out.println("<b> Email: </b> <input type=\"text\" name=\"email\" value=\"" + rs.getString("email") + "\"><br>");
                out.println("<input type=\"hidden\" name=\"action\" value=\"Update\"> ");
                out.println("<input type=\"submit\" value=\"Update\"> ");
                out.println("</form>");

                out.println("</body></html>");
            } else {
                // Display error message if record not found
                res.sendRedirect("studentsnew?errormsg=Record not found");
            }

        } catch (SQLException e) {
            res.sendRedirect("studentsnew?errormsg=" + e.getMessage());
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	req.setCharacterEncoding("utf-8");
        String action = req.getParameter("action");
        
        

        if ("Delete".equals(action)) {
            // Delete operation
            String idToDelete = req.getParameter("am");
            deleteStudentRecord(idToDelete);
            res.sendRedirect("studentsnew");
            return;
        } else if ("Update".equals(action)) {
            // Update operation
            updateStudentRecord(req, res);
            return;
        } else if ("Save".equals(action)) {
            // Save operation (add new student)
            saveNewStudent(req, res);
            return;
            
        }

        // Default action - Display the table
        doGet(req, res);
        
    }

    private void updateStudentRecord(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String qry = "UPDATE students SET first_name=?, last_name=?, semester=?, email=? WHERE id=?";

        Connection dbCon;

        res.setCharacterEncoding("utf-8");
        res.setContentType("text/html; charset=utf-8");


        String am = req.getParameter("am");
        String onoma = req.getParameter("onoma");
        String eponimo = req.getParameter("eponimo");
        String examino = req.getParameter("examino");
        String email = req.getParameter("email");

        try {

            Class.forName(driver);
            dbCon = DriverManager.getConnection(dbURL);

            PreparedStatement stmt;
            stmt = dbCon.prepareStatement(qry);
            stmt.setString(1, onoma);
            stmt.setString(2, eponimo);
            stmt.setString(3, examino);
            stmt.setString(4, email);
            stmt.setString(5, am);

            int i = stmt.executeUpdate();
            System.out.println("Updated " + i + " row(s)");

            res.sendRedirect("studentsnew");

        } catch (Exception e) {
        	res.sendRedirect("studentsnew?errormsg=" + java.net.URLEncoder.encode(e.getMessage(),"UTF-8"));
        }
    }

    private void saveNewStudent(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String qryCheckExistence = "SELECT id FROM students WHERE id=?";
        String qryInsert = "INSERT INTO students (id, first_name, last_name, semester, email) VALUES (?, ?, ?, ?, ?)";

        Connection dbCon;

        res.setCharacterEncoding("utf-8");
        res.setContentType("text/html; charset=utf-8");


        String am = req.getParameter("am");
        String onoma = req.getParameter("onoma");
        String eponimo = req.getParameter("eponimo");
        String examino = req.getParameter("examino");
        String email = req.getParameter("email");

        try {
            Class.forName(driver);
            dbCon = DriverManager.getConnection(dbURL);

            // Check if student ID already exists
            PreparedStatement checkStmt = dbCon.prepareStatement(qryCheckExistence);
            checkStmt.setString(1, am);
            ResultSet existingRecords = checkStmt.executeQuery();

            if (existingRecords.next()) {
                // Student ID already exists, redirect with error message
                res.sendRedirect("studentsnew?errormsg=Student ID already exists");
                return;
            }

            // If ID doesn't exist, proceed with insertion
            PreparedStatement insertStmt = dbCon.prepareStatement(qryInsert);
            insertStmt.setString(1, am);
            insertStmt.setString(2, onoma);
            insertStmt.setString(3, eponimo);
            insertStmt.setString(4, examino);
            insertStmt.setString(5, email);

            int i = insertStmt.executeUpdate();
            System.out.println("Inserted " + i + " row(s)");

            res.sendRedirect("studentsnew");

        } catch (Exception e) {
	
			res.sendRedirect("studentsnew?errormsg=" + 
		           java.net.URLEncoder.encode(e.getMessage(),"UTF-8"));
			
		}
    }


    private void deleteStudentRecord(String id) {
        String qry = "DELETE FROM students WHERE id = ?";

        try (Connection dbCon = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = dbCon.prepareStatement(qry)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void printForm(PrintWriter out) {
    	
        out.println("<form action=\"studentsnew\" method=\"POST\">");
        out.println("<b> Παρακαλώ δώστε τα ακόλουθα στοιχεία: </b> <br>");
        out.println("<b> Όνομα :  </b> <input type=\"text\" name=\"onoma\" ><br>");
        out.println("<b> Επώνυμο :  </b> <input type=\"text\" name=\"eponimo\" ><br>");
        out.println("<b> Αριθμός Μητρώου: </b> <input type=\"text\" name=\"am\" ><br>");
        out.println("<b> Εξάμηνο: </b>  <input type=\"text\" name=\"examino\" ><br>");
        out.println("<b> Email: </b> <input type=\"text\" name=\"email\" ><br>");
        out.println("<input type=\"hidden\" name=\"action\" value=\"Save\"> ");
        out.println("<input type=\"submit\" value=\"Save\"> ");
        out.println("</form>");
    }

    void printAnyError(PrintWriter out, HttpServletRequest req) {
        String errorMessage = req.getParameter("errormsg");
        if (errorMessage != null) {
            out.println("<br><strong style=\"color:red\"> Error: " + errorMessage + "</strong>");
        }
    }
}
