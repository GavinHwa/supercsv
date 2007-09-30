/*
 * SuperCSV is Copyright 2007, Kasper B. Graversen Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.test.supercsv.speedtests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.Tokenizer_if;
import org.supercsv.prefs.CsvPreference;

/**
 * Read speed-test the CSV against native String.split()
 * 
 * @author Kasper B. Graversen
 */
public class ReadingSpeedTest {
	public static final class RealBean {
		String name1, name2;
		long no, phone;
		Date date;

		public Date getDate() {
			return date;
		}

		public String getName1() {
			return name1;
		}

		public String getName2() {
			return name2;
		}

		public long getNo() {
			return no;
		}

		public long getPhone() {
			return phone;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public void setName1(String name1) {
			this.name1 = name1;
		}

		public void setName2(String name2) {
			this.name2 = name2;
		}

		public void setNo(long no) {
			this.no = no;
		}

		public void setPhone(long phone) {
			this.phone = phone;
		}
	}

	public static final class StringsBean {
		String no, name1, name2, phone, date;

		public String getDate() {
			return date;
		}

		public String getName1() {
			return name1;
		}

		public String getName2() {
			return name2;
		}

		public String getNo() {
			return no;
		}

		public String getPhone() {
			return phone;
		}

		public void setDate(final String date) {
			this.date = date;
		}

		public void setName1(final String name1) {
			this.name1 = name1;
		}

		public void setName2(final String name2) {
			this.name2 = name2;
		}

		public void setNo(final String no) {
			this.no = no;
		}

		public void setPhone(final String phone) {
			this.phone = phone;
		}
	}

	static final String[] header = new String[] { "no", "name1", "name2", "phone", "date" };
	static final CellProcessor[] processors = new CellProcessor[] { new ParseLong(), null, null, new ParseLong(), new ParseDate("dd/MM/yy") };

	public static final String TEST_FILE = "supercsv.speedtest.testfile.csv";
	public static final int LINES_IN_TEST_FILE = 250000;// 100000;

	public static final int TEST_RUNS = 5;// 1;

	@BeforeClass
	public static void createTestFile() throws Exception {
		final Writer w = new BufferedWriter(new FileWriter(TEST_FILE));
		for(int i = 0; i < LINES_IN_TEST_FILE; i++)
			w.write(Creators.createAnonymousLine());
		w.close();
	}

	//
	// @AfterClass
	// public static void tearDown() throws Exception {
	// final File f = new File(TEST_FILE);
	// f.delete();
	// }

	private String makeTableLine(String msg, long baseTime, long runTime) {
		return String.format("<tr><td>%s</td><td>%4.2f%%</td><td>%4.2f</td></tr>", msg, (100.0 * runTime) / baseTime, runTime / (TEST_RUNS * 1000.0));
	}

	private void readCsvBeanFullReader(final Reader r) throws Exception {
		final CsvBeanReader csv = new CsvBeanReader(r, CsvPreference.EXCEL_PREFERENCE);
		while(csv.read(RealBean.class, header, processors) != null) {
			// System.out.print("/");
		}
		r.close();
	}

	private void readCsvBeanReader(final Reader r) throws Exception {
		final CsvBeanReader csv = new CsvBeanReader(r, CsvPreference.EXCEL_PREFERENCE);
		while(csv.read(StringsBean.class, header) != null) {
			// System.out.print("/");
		}
		r.close();
	}

	private void readCsvListReader(final Reader r) throws Exception {
		final CsvListReader csv = new CsvListReader(r, CsvPreference.EXCEL_PREFERENCE);
		while(csv.read() != null) {
			// System.out.print("*");
		}
		r.close();
	}

	private void readCsvListReaderOldTokenizer(final Reader r) throws Exception {
		final CsvListReader csv = new CsvListReader(r, CsvPreference.EXCEL_PREFERENCE);
		csv.setTokenizer(new Tokenizer_if(r, CsvPreference.EXCEL_PREFERENCE));
		while(csv.read() != null) {
			// System.out.print("*");
		}
		r.close();
	}

	private void readCsvMapReader(final Reader r) throws Exception {
		final CsvMapReader csv = new CsvMapReader(r, CsvPreference.EXCEL_PREFERENCE);
		while(csv.read(header) != null) {
			// System.out.print(";");
		}
		r.close();
	}

	private void readNative(final BufferedReader r) throws Exception {
		String line;
		while((line = r.readLine()) != null) {
			// System.out.print(".");
		}
		r.close();
	}

	@Test
	public void readSpeedTest() throws Exception {
		final long[] times = { 0, 0, 0, 0, 0, 0 };
		for(int i = 0; i < TEST_RUNS; i++) {
			int testNo = 0;
			System.out.println(i);
			long t;
			t = System.currentTimeMillis();
			readNative(new BufferedReader(new FileReader(TEST_FILE)));
			times[testNo++] += System.currentTimeMillis() - t;

			t = System.currentTimeMillis();
			readCsvListReader(new FileReader(TEST_FILE));
			times[testNo++] += System.currentTimeMillis() - t;

			t = System.currentTimeMillis();
			readCsvListReaderOldTokenizer(new FileReader(TEST_FILE));
			times[testNo++] += System.currentTimeMillis() - t;

			// time only simple string
			// t = System.currentTimeMillis();
			// readCsvMapReader(new FileReader(TEST_FILE));
			// times[testNo++] += System.currentTimeMillis() - t;
			//
			// t = System.currentTimeMillis();
			// readCsvBeanReader(new FileReader(TEST_FILE));
			// times[testNo++] += System.currentTimeMillis() - t;
			//
			// t = System.currentTimeMillis();
			// readCsvBeanFullReader(new FileReader(TEST_FILE));
			// times[testNo++] += System.currentTimeMillis() - t;
		}
		{
			int i = 0;
			System.out.println("<table><tr><th>Method</th><th>Relative speed</th><th>Average execution time</th></tr>");
			System.out.println(makeTableLine("split()", times[0], times[i++]));
			System.out.println(makeTableLine("ListReader", times[0], times[i++]));
			System.out.println(makeTableLine("ListReader_old tokenizer", times[0], times[i++]));
			System.out.println(makeTableLine("MapReader", times[0], times[i++]));
			System.out.println(makeTableLine("BeanReader", times[0], times[i++]));
			System.out.println(makeTableLine("BeanReader (full)", times[0], times[i++]));
			System.out.println("</table>");
		}
	}
}
