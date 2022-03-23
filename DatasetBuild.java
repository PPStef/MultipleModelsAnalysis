import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import bd.ConnectionInfo;
import bd.DbOps;

public class DatasetBuild {

	public static ArrayList<Integer> getIds() throws ClassNotFoundException, SQLException {

		ArrayList<Integer> ids = new ArrayList<Integer>();
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/tr", "root", "");

		String query = "SELECT id from iddusers inner JOIN userrole on iddusers.id=userrole.userid "
				+ "INNER JOIN usersectii on userrole.userid=usersectii.userid "
				+ "where userrole.roleid = 4 and usersectii.sectieid=4 and usersectii.an =1";

		Statement stmt = con.createStatement();

		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while (rs.next()) {
				int x = rs.getInt(1);
				//  System.out.println("id gasit este: "+x);
				if (x > 168)
					ids.add(x);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.close();
		return ids;
	}

	public static ArrayList<Integer> getStudents() throws ClassNotFoundException, SQLException {

		ArrayList<Integer> idsF = new ArrayList<Integer>();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ids = getIds();
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/tr", "root", "");
		for (int i = 0; i < ids.size(); i++) {
			String query = "Select userid from activity where userid=" + ids.get(i) + " and actiune='termina test'";

			Statement stmt = con.createStatement();

			ResultSet rs = null;
			try {
				rs = stmt.executeQuery(query);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (rs.next()) {
					int x = rs.getInt(1);
					//System.out.println("id gasit este: "+x);
					idsF.add(x);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		con.close();
		return idsF;
	}

	
	public static ArrayList<Float> getGrades(int id) throws ClassNotFoundException, SQLException {
		//System.out.println("getMeanGrade");
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/tr", "root", "");

		ArrayList<Float> grades = new ArrayList<Float>();
		String query = "SELECT detalii FROM `activity` where actiune='termina test' and userid =" + id;

		Statement stmt = con.createStatement();
		ResultSet rs, rs1 = null;
		String x = new String();
		float nota = 0, nf = 0;
		//DecimalFormat numberFormat = new DecimalFormat("#.00");
		//System.out.println(numberFormat.format(number));
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			x = rs.getString(1);
			x = x.substring(10, x.length());
			nota = Float.parseFloat(x);
			grades.add(nota);
		}
		//for(int i=0;i<grades.size();i++)
		// System.out.println("nota "+i+" este "+grades.get(i)); 
		String query1 = "SELECT count(detalii) FROM `activity` where actiune='termina test' and userid =" + id;
		rs1 = stmt.executeQuery(query1);
		int noOfTests = 0;
		while (rs1.next()) {
			noOfTests = rs1.getInt(1);
		}
		//System.out.println("avem: "+noOfTests+"  teste si in array  "+grades.size());
		con.close();

		return grades;
	}

	public static float normalize(float x, float dataLow, float dataHigh) {
		return ((x - dataLow)
				/ (dataHigh - dataLow));
	}

	public static void buildArffTrends10(int noTests) throws ClassNotFoundException, SQLException, IOException {

		String outputFilePath = Paths.get("teste" + noTests + ".arff").toString();
		FileWriter fileWriter = new FileWriter(outputFilePath);
		BufferedWriter mWriter = new BufferedWriter(fileWriter);
		mWriter.write("@RELATION tesysARFF\n\n");
		mWriter.write("@ATTRIBUTE ID NUMERIC\n");
		for (int i = 0; i < noTests; i++)
			mWriter.write("@ATTRIBUTE T" + (i + 1) + " NUMERIC\n");
		/*	mWriter.write("@ATTRIBUTE T1 NUMERIC\n");
			mWriter.write("@ATTRIBUTE T2 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T3 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T4 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T5 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T6 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T7 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T8 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T9 NUMERIC \n");
			mWriter.write("@ATTRIBUTE T10 NUMERIC \n");*/
		mWriter.write("@ATTRIBUTE AVG NUMERIC \n");
		mWriter.write("@ATTRIBUTE AVGN NUMERIC \n");
		mWriter.write("@ATTRIBUTE AVGND {LOW, AVERAGE, HIGH} \n");
		mWriter.write("@ATTRIBUTE TREND NUMERIC \n");
		mWriter.write("@ATTRIBUTE TRENDD {D, S, A} \n");

		mWriter.write("\n\n@DATA\n");
		ArrayList<Integer> ids = getStudents();
		ArrayList<Float> grades;
		ArrayList<Float> avgs = new ArrayList<Float>();
		float dataHigh = 0, dataLow = 20, avgn;
		int idu;
		int r = 0;
		for (int i = 0; i < ids.size(); i++) {
			idu = ids.get(i);
			ArrayList<Float> grade = getGrades(idu);
			if (grade.size() >= 10) {
				float a = 0;
				for (int k = 0; k < noTests; k++)
					a += grade.get(k);
				a /= noTests;
				avgs.add(r, a);
				r++;

			}
		}

		for (int j = 0; j < avgs.size(); j++) {
			if (dataHigh < avgs.get(j))
				dataHigh = avgs.get(j);

			if (dataLow > avgs.get(j))
				dataLow = avgs.get(j);
		}

		for (int i = 0; i < ids.size(); i++) {
			int id = ids.get(i);

			ArrayList<Float> gr = getGrades(id);
			if (gr.size() >= 10) {
				//System.out.println("ID= "+id);
				grades = gr;

				float avg = 0;
				for (int k = 0; k < noTests; k++)
					avg += grades.get(k);
				avg /= noTests;
				avgn = normalize(avg, dataLow, dataHigh);
				System.out.println(
						"avgn = " + avgn + "   avg = " + avg + "  dataLow=  " + dataLow + "  dataHigh=  " + dataHigh);
				String s;
				if (avgn < 0.33)
					s = "LOW";
				else if (avgn >= 0.33 && avgn < 0.66)
					s = "AVERAGE";
				else
					s = "HIGH";
				int count = 0;
				float trend;
				for (int p = 0; p < noTests - 1; p++)
					if (grades.get(p) < grades.get(p + 1))
						count++;
				trend = (float) count / noTests;

				String t;

				if (trend < 0.33)
					t = "D";
				else if (avgn >= 0.33 && avgn < 0.66)
					t = "S";
				else
					t = "A";

				//System.out.println("count = "+count+"  noTests=  "+noTests+"  trend=  "+trend);
				mWriter.write("" + id + ",");
				for (int v = 0; v < noTests; v++)
					mWriter.write("" + String.format("%.2f", grades.get(v)) + ",");

				mWriter.write("" + String.format("%.2f", avg) + ","
						+ String.format("%.2f", avgn) + ","
						+ s + "," + String.format("%.2f", trend) + "," + t + "\n");
			}
		}
		mWriter.close();
		System.out.println("Ajunsaram la final cu testele");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ConnectionInfo connectionInfo = new ConnectionInfo("root", "", "tr");
		try {
			DbOps.createInstance(connectionInfo);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
			for (int i = 5; i < 11; i++)
				try {
					buildArffTrends10(i);
				} catch (ClassNotFoundException | SQLException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		
		
	}

}
