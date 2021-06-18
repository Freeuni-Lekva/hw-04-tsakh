// Cracker.java
/*
 Generates SHA hashes of short strings in parallel.
*/

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Cracker {
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();
	private CountDownLatch countDown;
	private List<String> results;

	class Worker extends Thread{
		private int startIndx, endIndx, length;
		private String target;
		public Worker(int start, int end, String target, int length){
			startIndx = start;
			endIndx = end;
			this.length = length;
			this.target = target;
		}
		@Override
		public void run(){
			for(int i = startIndx; i < endIndx; i++){
				StringBuilder builder = new StringBuilder();
				builder.append(CHARS[i]);
				try {
					rec(builder);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
			countDown.countDown();
		}

		private void tryNewSeq(StringBuilder builder, int i) throws NoSuchAlgorithmException {
			builder.append(CHARS[i]);
			int lastIndx = builder.toString().length() - 1;
			rec(builder);
			builder.deleteCharAt(lastIndx);
		}

		private void rec (StringBuilder builder) throws NoSuchAlgorithmException {
			if(builder.toString().length() > length) return;
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(builder.toString().getBytes());
			//System.out.println(builder.toString() + " +++");
			if(Arrays.equals(md.digest(), hexToArray(target))){
				System.out.println(builder.toString());
				results.add(builder.toString());
				//return;
			}
			for (int i = 0; i < CHARS.length; i++){
				tryNewSeq(builder, i);
			}
		}
	}

	public List<String> getResults(){
		return results;
	}

	public Cracker(int numThreads){
		countDown = new CountDownLatch(numThreads);
		results = new ArrayList<>();
	}
	
	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i=0; i<hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}

	public void processPasswords(String target, int maxLength, int numThreads) throws InterruptedException {
		int start = 0;
		int end = 0;
		for (int i = 0; i < numThreads; i++){
			if(i == numThreads - 1){
				end = CHARS.length;
			} else {
				end += CHARS.length / numThreads;
			}
			Worker curr = new Worker(start, end, target, maxLength);
			curr.start();
			start = end;
		}
		countDown.await();
	}

	public String getHash(String s) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA");
		byte[] param = s.getBytes();
		return hexToString(md.digest(param));
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException {
		if (args.length == 1) {
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] param = args[0].getBytes();
			System.out.println(hexToString(md.digest(param)));
			return;
		}
		if (args.length < 2) {
			System.out.println("Args: target length [workers]");
			return;
			//System.exit(1);
		}
		// args: targ len [num]
		String targ = args[0];
		int len = Integer.parseInt(args[1]);
		int num = 1;
		if (args.length>2) {
			num = Integer.parseInt(args[2]);
		}
		// a! 34800e15707fae815d7c90d49de44aca97e2d759
		// xyz 66b27417d37e024c46526c2f6d358a754fc552f3
		Cracker c = new Cracker(num);
		c.processPasswords(targ, len, num);
		System.out.println("all done");
	}
}
