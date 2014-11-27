import java.sql.*;
import java.io.*;
import java.util.*;

public class TestMailOrder {
	private final static String URL = "jdbc:oracle:thin:@dataserv.mscs.mu.edu:1521:orcl";

	@SuppressWarnings("resource")
	public static void main(String[] args) throws SQLException, IOException {
		String user, pwd;
		try {Class.forName ("oracle.jdbc.driver.OracleDriver");} catch (ClassNotFoundException e)
		{System.out.println ("Could not load the driver: " + e.getMessage());}
		Scanner kb = new Scanner(System.in);
		System.out.print("Please enter database user name: ");
		user = kb.next();
		System.out.print("Please enter database password: ");
		pwd = kb.next();

		Connection conn = DriverManager.getConnection (URL, user, pwd);
		Statement cstmrInfo = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
		Statement ordrInfo = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
		while(true){
			System.out.print("Please enter your order number, or 'exit' to quit: ");
			String ono = kb.next();
			ono = ono.replaceAll("\\s", ""); //remove extra spaces if entered
			if (ono.equalsIgnoreCase("exit")){
				conn.close();
				System.out.println("Exiting mail order Look Up. All connections have been closed");
				System.exit(1);
			}

			try{
				ResultSet rset = cstmrInfo.executeQuery ("select c.cname, c.cno, c.zip, e.ename, e.eno, o.received, o.shipped from employees e, customers c, orders o where o.ono = "+ono +" AND o.cno = c.cno and o.eno = e.eno");
				ResultSet rset2 = ordrInfo.executeQuery("select p.pno, p.pname, od.qty, p.price,  (p.price * od.qty) from parts p, odetails od where od.ono = "+ono +" and od.pno = p.pno");
				displayOrderDetails(rset, ono);
				getOrderedPartsDetails(rset2);
				rset.close();
				rset2.close();
			}catch(SQLException e){System.out.printf("Invoice for order %s does not exist in the system. \n\n", ono);};

		}
	}
	/*
	 * ===============================================================================================================
	 * =============================================HELPER FUNCTION SECTION===========================================
	 * ==================================Designed to keep main as clean as possible===================================
	 * ===============================================================================================================
	 */

	//	private static int getRowCount(ResultSet rset) throws SQLException {
	//		int rowCount = 0;
	//		while (rset.next())  
	//		{rowCount++;}  
	//		return rowCount;
	//	}

	/**
	 * Gets parts order details from the result set
	 * @param rset Result Set
	 * @param nRows Int value indicating number of rows in the result set
	 * @return Double value indicating total cost
	 * @throws SQLException
	 */
	private static void getOrderedPartsDetails(ResultSet rset) throws SQLException {	
		double total = 0;
		System.out.printf("Part No\t PartName\t\tQuantity Price   Sum\n");
		System.out.printf("------------------------------------------------------\n");
		rset.beforeFirst();
		while(rset.next())//Go through every row to print details
		{
			//See getStringFormat method for details
			System.out.printf(getStringFormat(rset.getString(2).length()), rset.getString(1), rset.getString(2), rset.getString(3), rset.getString(4), rset.getString(5));
			total += rset.getDouble(5);
		}
		System.out.println("------------------------------------------------------" +
				"\nTotal: " +total+"\n");

	}

	/**
	 * Aesthetics method to determine number of tabs required for good looking parts details print
	 * @param length Length of the Part Name string
	 * @return format of printf
	 */
	private static String getStringFormat(int length) {
		String format = "%s\t %s\t %s\t %s\t %s\n"; //Printf format if Part name is between 16 and 30 characters
		if(length<15)
			format = "%s\t %s\t\t %s\t %s\t %s\n"; //Printf format if Part name is between 6 and 15 characters
		else if (length<6)
			format = "%s\t %s\t\t\t %s\t %s\t %s\n"; //printf format if Part name is between 1 and 5 characters
		return format;
	}
	/**
	 * Print order details, Order number, Customer name, Customer Number, Zip code,
	 * Employee who took the order (with emp ID), when was item received, and when the item was shipped
	 * 
	 * @param rset Result set from query 2
	 * @param ono Order Number
	 * @throws SQLException
	 */
	private static void displayOrderDetails(ResultSet rset, String ono) throws SQLException {

		rset.first();//There will only be 1 result row. Same as rset.absolute(1)
		String testShip = rset.getString(7)+"", testRec = rset.getString(6)+"";//Test strings to see if Shipped or Receive data cells are empty

		if(testShip.equalsIgnoreCase("null"))//Order details if there is no shipment date
			System.out.printf("\nOrder Number: %s\nCustomer: %s\nCustomer No: %s\nZip: %s\nTaken By: %s (emp. No. %s)\nReceived on: %s\nNote: Store transaction\n", ono, rset.getString(1), rset.getString(2), rset.getString(3), rset.getString(4), rset.getString(5), rset.getDate(6) );
		else if(testRec.equalsIgnoreCase("null"))//Order details if there is no receive date
			System.out.printf("\nOrder Number: %s\nCustomer: %s\nCustomer No: %s\nZip: %s\nTaken By: %s (emp. No. %s)\nReceived on: %s\nShipped on: %s\n", ono, rset.getString(1), rset.getString(2), rset.getString(3), rset.getString(4), rset.getString(5), "Customer didn't receive his package yet", rset.getDate(7));
		else //regular Order details as shown in the assignment example
			System.out.printf("\nOrder Number: %s\nCustomer: %s\nCustomer No: %s\nZip: %s\nTaken By: %s (emp. No. %s)\nReceived on: %s\nShipped on: %s\n", ono, rset.getString(1), rset.getString(2), rset.getString(3), rset.getString(4), rset.getString(5), rset.getDate(6), rset.getDate(7));
	}


}

