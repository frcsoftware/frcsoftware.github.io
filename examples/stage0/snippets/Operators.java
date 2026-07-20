/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
// [variables]
int answer1 = 2 + 4;
int answer2 = 6 / 3;
int answer3 = 10 - 3;
// [/variables]

void main() {
    // [multiplication]
    int magicNumber = 6;
    System.out.print(magicNumber * 2);
    // [/multiplication]

    // [increments]
    int x = 6;
    int y = 7;

    System.out.println(x++);
    System.out.println(y--);
    // [/increments]

    // [arithmetic]
    int a = 10;
    int b = 5;
    a += 1;
    b -= 1;

    System.out.println(a); // prints 11
    System.out.println(b); // prints 4
    // [/arithmetic]

    // [comparison]
    int c = 2;
    int d = 4;
    System.out.print(c > d);
    // [/comparison]

    // [logical]
    boolean fiveIsGreaterThanThree = 5 > 3; // True
    boolean nineIsLessThanTwo = 9 < 2; // False

    System.out.println(fiveIsGreaterThanThree && nineIsLessThanTwo);
    System.out.println(fiveIsGreaterThanThree || nineIsLessThanTwo);
    System.out.println(!fiveIsGreaterThanThree);
    // [/logical]
}
