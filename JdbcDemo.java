//STEP 1. Import required packages

import java.sql.*;
import java.util.Scanner;


public class JdbcDemo {

//Set JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
//static final String DB_URL = "jdbc:mysql://localhost/companydb";
   static final String DB_URL = "jdbc:mysql://localhost?useSSL=false";
//  Database credentials
   static final String USER = "root";// add your user 
   static final String PASS = "";// add password


   public static void insertIntoBank(CreateTable bank, String data){
      bank.insertRecord("(Bank_name)", data);
   }

   public static void insertIntoBranch(CreateTable branch, String data){
      branch.insertRecord("(Branch_code, Branch_address, Branch_city, Bank_id)", data);
   }

   public static int insertIntoCustomer(CreateTable customer, String data){
      int status = customer.insertRecord("(Customer_number, Customer_name, Customer_address, Customer_city)", data);
      return status;
   }

   public static int insertIntoAccount(CreateTable account, String data){
      int status = account.insertRecord("(Account_balance, Branch_id, Customer_number)", data);
      return status;
   }

   public static int insertIntoTransaction(CreateTable transaction, String data){
      int status = transaction.insertRecord("(Txn_amount, SavAccount_number)", data);
      return status;
   }

   public static void main(String[] args) {
   Connection conn = null;
   ResultSet set = null;
   Statement stmt = null;
   Scanner scanner = new Scanner(System.in);

   int id=1;
   String BankInfo="", BranchInfo="", AccountInfo="", CustomerInfo="", AccOwnerInfo="", SavingsInfo="", CurrentInfo="", DepositInfo="", TransactionInfo="";


// STEP 2. Connecting to the Database
   try{
      //STEP 2a: Register JDBC driver
      Class.forName(JDBC_DRIVER);
      //STEP 2b: Open a connection
      System.out.println("Connecting to database...");
      conn = DriverManager.getConnection(DB_URL,USER,PASS);
      //STEP 2c: Execute a query
      stmt = conn.createStatement();

      CreateDatabase createDatabase = new CreateDatabase("BankDB");
      int active = createDatabase.loadDB(stmt);

      if(active == 0) {
         BankInfo = "(" +
                 "Bank_id int auto_increment," +
                 "Bank_name varchar(30) unique not null," +
                 "PRIMARY KEY (Bank_id)" +
                 ")";
         BranchInfo = "(" +
                 "Branch_id int auto_increment," +
                 "Branch_code int unique not null," +
                 "Branch_address varchar(40) unique not null," +
                 "Branch_city varchar(30) not null," +
                 "Bank_id int not null," +
                 "PRIMARY KEY (Branch_id)" +
                 ")";
         AccountInfo = "(" +
                 "Account_number int unique not null auto_increment," +
                 "Account_balance float not null," +
                 "Branch_id int not null," +
                 "Customer_number varchar(10) not null," +
                 "PRIMARY KEY (Account_number)" +
                 ")";
         CustomerInfo = "(" +
                 "Customer_id int auto_increment," +
                 "Customer_number varchar(10) unique not null," +
                 "Customer_name varchar(30) not null," +
                 "Customer_address varchar(50) not null," +
                 "Customer_city varchar(20) not null," +
                 "PRIMARY KEY (Customer_id)" +
                 ")";
         TransactionInfo = "(" +
                 "Txn_id int auto_increment," +
                 "Txn_amount float not null," +
                 "Txn_date datetime default now()," +
                 "SavAccount_number int not null," +
                 "PRIMARY KEY (Txn_id)" +
                 ")";
      }

      CreateTable bank = new CreateTable("BANK", BankInfo, stmt, active);
      CreateTable branch = new CreateTable("BRANCH", BranchInfo, stmt, active);
      CreateTable account = new CreateTable("ACCOUNT", AccountInfo, stmt, active);
      CreateTable customer = new CreateTable("CUSTOMER", CustomerInfo, stmt, active);
      CreateTable transaction = new CreateTable("TRANSACTION", TransactionInfo, stmt, active);

      if(active == 0) {
         branch.addForeignConstraint("Bank_id", "Bank_id", "BANK");
         account.addForeignConstraint("Branch_id", "Branch_id", "BRANCH");
         account.addForeignConstraint("Customer_number", "Customer_number", "CUSTOMER");
         transaction.addForeignConstraint("SavAccount_number", "Account_number", "ACCOUNT");

         stmt.executeUpdate("alter table ACCOUNT auto_increment=100000");

         insertIntoBank(bank, "('HDFC Bank')");
         insertIntoBank(bank, "('Bank Of Baroda')");

         insertIntoBranch(branch, "(100, 'Church Street', 'Bangalore', 1)");
         insertIntoBranch(branch, "(101, 'Rajwada', 'Indore', 1)");
         insertIntoBranch(branch, "(200, 'Marathahalli', 'Bangalore', 2)");
         insertIntoBranch(branch, "(201, 'Vijay Nagar', 'Indore', 2)");
      }
      char another;
      String sql="";
      do {
         System.out.println("\n1. Create Bank Account\n2. Update Account Information\n3. Read Account Information\n4. Make a Transaction\n5. Delete Bank Account");
         System.out.print("\n Choose an option: ");
         int option = scanner.nextInt();
         if (option < 1 || option > 5)
            System.out.println("Invalid Option");
         else {
            if (option == 1) {
               System.out.println("\n\tBank List");
               System.out.println("1. HDFC Bank\n2. Bank Of Baroda\n");
               int bankID;
               do {
                  System.out.print("Choose a Bank: ");
                  bankID = scanner.nextInt();
                  if (bankID < 1 || bankID > 2)
                     System.out.println("Invalid Bank ID");
               } while (bankID < 1 || bankID > 2);

               System.out.println("\n\tBranch List");
               sql = "select Branch_code, Branch_address, Branch_city from BRANCH where Bank_id=" + bankID;
               set = stmt.executeQuery(sql);
               while (set.next())
                  System.out.println(set.getString("Branch_code") + ". " + set.getString("Branch_address") + ", " + set.getString("Branch_city"));

               System.out.print("Choose a Branch Code: ");
               int branchCode = scanner.nextInt();

               String c;
               System.out.println("\n\tCustomer Details");
               System.out.print("Enter your Name: ");
               c = scanner.nextLine();
               String name = scanner.nextLine();
               System.out.print("Enter your Address: ");
               String address = scanner.nextLine();
               System.out.print("Enter your City: ");
               String city = scanner.nextLine();

               int status = 0;
               do {
                  System.out.print("Enter your Contact Number: ");
                  String phone = scanner.next();
                  if(phone.length() != 10)
                  {
                     System.out.println("Phone number must be of length 10");
                  }
                  else
                  {
                  set = stmt.executeQuery("select count(*) from CUSTOMER where Customer_number=" + phone);
                  if (set.next())
                     status = set.getInt("count(*)");
                  if (status == 0) {
                     insertIntoCustomer(customer, "(\'" + phone + "\', \'" + name + "\', \'" + address + "\', \'" + city + "\')");
                     float balance;
                     do {
                        System.out.print("\nEnter the initial balance you want to keep in your account (min balance: Rs.2000): ");
                        balance = scanner.nextFloat();

                        if (balance < 2000)
                           System.out.println("Minimum balance must be Rs.2000");
                     } while (balance < 2000);
                     System.out.println("Balance: " + balance);
                     set = stmt.executeQuery("select Branch_id from BRANCH where Branch_code=" + branchCode);
                     int branchID = 0;
                     int accNo = 0;
                     while (set.next())
                        branchID = set.getInt("Branch_id");
                     System.out.println("Branch_id=" + branchID);
                     int stat = insertIntoAccount(account, "(" + balance + ", " + branchID + ", " + "\'" + phone + "\'" + ")");
                     if (stat == 0) {
                        set = stmt.executeQuery("select Account_number from ACCOUNT where Customer_number=" + phone);
                        if (set.next())
                           accNo = set.getInt("Account_number");
                        System.out.println("Account Created Successfully!!!");
                        set = stmt.executeQuery("select Bank_name from BANK where Bank_id=" + bankID);
                        if (set.next())
                           System.out.println("\nBank Name: " + set.getString("Bank_name"));
                        set = stmt.executeQuery("select Branch_address, Branch_city from BRANCH where Branch_code=" + branchCode);
                        if (set.next())
                           System.out.println("Branch Address: " + set.getString("Branch_address") + ", " + set.getString("Branch_city"));
                        System.out.println("Account Number: " + accNo);
                        System.out.println("Account Balance: " + balance);
                        System.out.println("Customer Name: " + name);
                        System.out.println("Customer Contact Number: " + phone);
                        System.out.println("Customer Address: " + address);
                        System.out.println("Customer City: " + city);
                     } else
                        System.out.println("Error in Creating Account");
                  } else {
                     System.out.println("Phone number already exists");
                  }
               }
               } while (status != 0);
            } else if (option == 2) {
               System.out.println("\n1. Update Customer Information\n2. Update Account Balance");
               System.out.print("Choose an option: ");
               int chooseUpdate = scanner.nextInt();

               if (chooseUpdate == 1) {
                  int count = 0;
                  do {
                     System.out.print("Enter your Account Number: ");
                     int accNo = scanner.nextInt();
                     try {
                        set = stmt.executeQuery("select count(*) from ACCOUNT where Account_number=" + accNo);
                        if (set.next())
                           count = set.getInt("count(*)");
                        if (count == 1) {
                           String phone = "";
                           System.out.println("\n1. Update your Name\n2. Update your City\n3. Update your address");
                           System.out.print("Choose an option: ");
                           int updateCust = scanner.nextInt();
                           set = stmt.executeQuery("select Customer_number from ACCOUNT where Account_number=" + accNo);
                           if (set.next())
                              phone = set.getString("Customer_number");

                           if (updateCust == 1) {
                              String name;
                              System.out.print("Enter your new Name: ");
                              String c = scanner.nextLine();
                              name = scanner.nextLine();
                              try {
                                 stmt.executeUpdate("update CUSTOMER set Customer_name=" + "\'" + name + "\' where Customer_number=" + phone);
                                 System.out.println("Name Updated Successfully");
                              } catch (Exception e) {
                                 System.out.println("Unable to update Name");
                              }
                           } else if (updateCust == 2) {
                              String city;
                              System.out.print("Enter your new City: ");
                              String c = scanner.nextLine();
                              city = scanner.nextLine();
                              try {
                                 stmt.executeUpdate("update CUSTOMER set Customer_city=" + "\'" + city + "\' where Customer_number=" + phone);
                                 System.out.println("City Updated Successfully");
                              } catch (Exception e) {
                                 System.out.println("Unable to update City");
                              }
                           } else if (updateCust == 3) {
                              String address;
                              System.out.print("Enter your new Address: ");
                              String c = scanner.nextLine();
                              address = scanner.nextLine();
                              try {
                                 stmt.executeUpdate("update CUSTOMER set Customer_address=" + "\'" + address + "\' where Customer_number=" + phone);
                                 System.out.println("Address Updated Successfully");
                              } catch (Exception e) {
                                 System.out.println("Unable to update Address");
                              }
                           }
                        } else
                           System.out.println("Account Number doesn't exist!!!");
                     } catch (Exception e) {
                        System.out.println("Some Error occurred!! Try Again");
                     }
                  } while (count != 1);
               } else if (chooseUpdate == 2) {
                  int count = 0;
                  do {
                     System.out.print("Enter your Account Number: ");
                     int accNo = scanner.nextInt();
                     try {
                        set = stmt.executeQuery("select count(*) from ACCOUNT where Account_number=" + accNo);
                        if (set.next())
                           count = set.getInt("count(*)");
                        if (count == 1) {
                           float balance = 0;
                           System.out.print("Enter your New Balance: ");
                           balance = scanner.nextFloat();
                           try {
                              stmt.executeUpdate("update ACCOUNT set Account_balance=" + balance + " where Account_number=" + accNo);
                              System.out.println("Balance Updated Successfully");
                           } catch (Exception e) {
                              System.out.println("Unable to update balance");
                           }
                        } else
                           System.out.println("Account Number doesn't exist!!!");
                     } catch (Exception e) {
                        System.out.println("Some Error occurred!! Try Again");
                     }
                  } while (count != 1);
               }
            } else if (option == 3) {
               int count = 0;
               do {
                  System.out.print("\nEnter your Account Number: ");
                  int accNo = scanner.nextInt();
                  try {
                     set = stmt.executeQuery("select count(*) from ACCOUNT where Account_number=" + accNo);
                     if (set.next())
                        count = set.getInt("count(*)");
                     if (count == 1) {
                        int bankID = 0, branchID = 0;
                        set = stmt.executeQuery("select Branch_id from ACCOUNT where Account_number=" + accNo);
                        if (set.next())
                           branchID = set.getInt("Branch_id");
                        set = stmt.executeQuery("select Bank_id from BRANCH where Branch_id=" + branchID);
                        if (set.next())
                           bankID = set.getInt("Bank_id");
                        set = stmt.executeQuery("select Bank_name from BANK where Bank_id=" + bankID);
                        if (set.next())
                           System.out.println("\nBank Name: " + set.getString("Bank_name"));
                        set = stmt.executeQuery("select Branch_code, Branch_address, Branch_city from BRANCH where Branch_id=" + branchID);
                        if (set.next()) {
                           System.out.println("Branch Code: " + set.getInt("Branch_code"));
                           System.out.println("Branch Address: " + set.getString("Branch_address"));
                           System.out.println("Branch City: " + set.getString("Branch_city"));
                        }
                        System.out.println("Account Number: " + accNo);
                        set = stmt.executeQuery("select Account_balance, Customer_number from ACCOUNT where Account_number=" + accNo);
                        if (set.next()) {
                           System.out.println("Account Balance: " + set.getFloat("Account_balance"));
                           System.out.println("Contact Number: " + set.getString("Customer_number"));
                        }
                     } else
                        System.out.println("Account Number doesn't exist!!!");
                  } catch (SQLException throwables) {
                     System.out.println("Unable to read Account Information");
                  }
               } while (count != 1);
            } else if (option == 4) {
               int count = 0;
               do {
                  System.out.print("Enter your Account Number: ");
                  int accNo = scanner.nextInt();
                  try {
                     set = stmt.executeQuery("select count(*) from ACCOUNT where Account_number=" + accNo);
                     if (set.next())
                        count = set.getInt("count(*)");
                     if (count == 1) {
                        System.out.print("Enter Transaction Amount: ");
                        float amt = scanner.nextFloat();
                        float balance = 0;
                        set = stmt.executeQuery("select Account_balance from ACCOUNT where Account_number=" + accNo);
                        if (set.next()) {
                           balance = set.getFloat("Account_balance");
                        }
                        if ((balance - 2000) >= amt) {
                           int stat = insertIntoTransaction(transaction, "(" + amt + ", " + accNo + ")");
                           if (stat == 0) {
                              stmt.executeUpdate("update ACCOUNT set Account_balance=" + (balance - amt) + " where Account_number=" + accNo);
                              System.out.println("Transaction Successful");
                           }
                        } else {
                           System.out.println("Insufficient Balance");
                        }
                     } else
                        System.out.println("Account Number doesn't exist!!!");
                  } catch (Exception e) {
                     System.out.println("Some Error occurred!! Try Again");
                  }
               } while (count != 1);
            } else if (option == 5) {
               int count = 0;
               do {
                  System.out.print("Enter your Account Number: ");
                  int accNo = scanner.nextInt();
                  try {
                     set = stmt.executeQuery("select count(*) from ACCOUNT where Account_number=" + accNo);
                     if (set.next())
                        count = set.getInt("count(*)");
                     if (count == 1) {
                        String phone = "";
                        stmt.executeUpdate("delete from TRANSACTION where SavAccount_number=" + accNo);
                        set = stmt.executeQuery("select Customer_number from ACCOUNT where Account_number=" + accNo);
                        if (set.next())
                           phone = set.getString("Customer_number");
                        stmt.executeUpdate("delete from ACCOUNT where Account_number=" + accNo);
                        stmt.executeUpdate("delete from CUSTOMER where Customer_number=" + phone);
                        System.out.println("Account Deleted Successfully");
                     } else
                        System.out.println("Account Number doesn't exist!!!");
                  } catch (SQLException throwables) {
                     System.out.println("Unable to delete Account");
                  }
               } while (count != 1);
            }
         }
         System.out.print("\nWant to continue with application (y/n)?: ");
         another = scanner.next().charAt(0);
      }while(another == 'y');

      stmt.close();
      conn.close();
	}catch(SQLException se){    	 //Handle errors for JDBC
      	se.printStackTrace();
   	}catch(Exception e){        	//Handle errors for Class.forName
      e.printStackTrace();
   }finally{				//finally block used to close resources
      try{
         if(stmt!=null)
            stmt.close();
      }catch(SQLException se2){
      }
      try{
         if(conn!=null)
            conn.close();
      }catch(SQLException se){
         se.printStackTrace();
      }					//end finally try
   }					//end try
   System.out.println("End of Code");
}					//end main
}					//end class
