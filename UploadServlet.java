import java.io.*;
import java.util.*;
 
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
 
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.*;

public class UploadServlet extends HttpServlet {
   
   private boolean isMultipart;
   private String filePath;
   private String textfilename;
   private int maxFileSize = 50 * 1024;
   private int maxMemSize = 4 * 1024;
   private File file ;
   private ArrayList<String> file_uploads=new ArrayList<>();
   private String out_txt_file_name;
   private String resultName;
  


   public void init( ){
      // Get the file location where it would be stored.
      filePath = 
             getServletContext().getInitParameter("file-upload"); 
   }
   public void doPost(HttpServletRequest request, 
               HttpServletResponse response)
              throws ServletException, java.io.IOException {
               file_uploads.removeAll(file_uploads);

      // Check that we have a file upload request
                java.io.PrintWriter out = response.getWriter( );
      isMultipart = ServletFileUpload.isMultipartContent(request);
      response.setContentType("text/html");
      if( !isMultipart ){
         out.println("<html>");
         out.println("<head>");
         out.println("<title>Servlet upload</title>");  
         out.println("</head>");
         out.println("<body>");
         out.println("<p>No file uploaded</p>"); 
         out.println("</body>");
         out.println("</html>");
         return;
      }
      DiskFileItemFactory factory = new DiskFileItemFactory();
      // maximum size that will be stored in memory
      factory.setSizeThreshold(maxMemSize);
      // Location to save data that is larger than maxMemSize.
      factory.setRepository(new File("C:\\temp"));

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);
      // maximum file size to be uploaded.
      upload.setSizeMax( maxFileSize );

      try{ 
      // Parse the request to get file items.
      List fileItems = upload.parseRequest(request);
	
      // Process the uploaded file items
      Iterator i = fileItems.iterator();

      out.println("<html>");
      out.println("<head>");
      out.println("<title>Servlet upload</title>");  
      out.println("</head>");
      out.println("<body>");
      while ( i.hasNext () ) 
      {
         FileItem fi = (FileItem)i.next();
         if ( !fi.isFormField () )	
         {
            // Get the uploaded file parameters
            String fieldName = fi.getFieldName();
            String fileName = fi.getName();
            String contentType = fi.getContentType();
            boolean isInMemory = fi.isInMemory();
            long sizeInBytes = fi.getSize();
            // Write the file
            if( fileName.lastIndexOf("\\") >= 0 ){
               file = new File( filePath + 
               fileName.substring( fileName.lastIndexOf("\\"))) ;
            }else{
               file = new File( filePath + 
               fileName.substring(fileName.lastIndexOf("\\")+1)) ;
            }
            fi.write( file ) ;
            file_uploads.add(fileName);
            out.println("Uploaded Filename: " + fileName + "<br>");
         }
      }
      
   }catch(Exception ex) {
       System.out.println(ex);
   }
      tessarect();
      retrieveData();
      out.println(resultName);
      out.println("</body>");
      out.println("</html>");
   }
   public void doGet(HttpServletRequest request, 
                       HttpServletResponse response)
        throws ServletException, java.io.IOException {
        
        throw new ServletException("GET method used with " +
                getClass( ).getName( )+": POST method required.");
   }
   public void tessarect(){
        String program="tesseract";
        System.out.println(program);
        String image_file_name = "\"C:\\Program Files\\apache-tomcat-8.0.33\\webapps\\FileUpload\\Data\\";
        out_txt_file_name = "\"C:\\Program Files\\apache-tomcat-8.0.33\\webapps\\FileUpload\\ConvertedFiles\\";
        String filename = "";
        try{
            for(int i=0;i<file_uploads.size();i++)
            {
               filename = file_uploads.get(i);
               String command = program + " " + image_file_name + filename + "\" " + out_txt_file_name + filename +"\"" ;
               textfilename=out_txt_file_name + filename +".txt"+"\"" ;
               System.out.println(command);
               ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
               builder.redirectErrorStream(true);
               Process p = builder.start();
               BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
               String line;
               while (true) {
                  line = r.readLine();
                  if (line == null) { break; }
                  System.out.println(line);
               }
            }
         }catch(Exception e){

         }

   }
   public void retrieveData()
   {
      try{
            Class.forName("org.postgresql.Driver");
            Connection con=DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres","root");
            if(con!=null)
               System.out.print("Connection established");
            textfilename = textfilename.substring(1, textfilename.length() -1);
            BufferedReader reader=new BufferedReader(new FileReader(textfilename));
           //One way of reading the file
            System.out.println("Reading the file using readLine() method:");
            String contentLine = reader.readLine();
            System.out.println(contentLine);
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery("select * from vehicleData ");
            while(rs.next())
            {
               String regNum = rs.getString(1).replaceAll("\\r|\\n|\\s", "");
               if(contentLine.equals(regNum)){
                  resultName=rs.getString(2);
                  System.out.println("Reg Number match!  Name of person assigned to: " + resultName);
               }else{
                  resultName="Invalid reg num";
                  System.out.println(resultName + " --- "+rs.getString(1));
               }
            }
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
   }
}