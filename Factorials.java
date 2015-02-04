import java.util.Scanner;
public class Factorial {

		public static void main(String[] args) {
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Please enter an integer to calculate it's factorial:");
			int mynumber = keyboard.nextInt();
			System.out.println("factorial(" + mynumber + ") =" + factorial(mynumber));
		}
		
		/**
		 * Takes an integer x and returns x!
		 * f(1) = 1, f(x) = x * f(x - 1)
		 * @param x the number to factorial
		 * @return x!
		 */
		public static int factorial(int x) {
			int factorial = x;
			
			for (int y = (x - 1); y > 1; y--) {
				factorial = y * factorial;
			}
			
			return factorial;
		}
	}
