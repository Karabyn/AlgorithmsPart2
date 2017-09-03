package SeamCarver;

import edu.princeton.cs.algs4.Picture;

import java.awt.*;


/**
 * Algorithms Part II by Princeton University
 * Programming assignment 2. SeamCarver.
 * http://coursera.cs.princeton.edu/algs4/assignments/seamCarving.html
 * Petro Karabyn.
 * 27-Aug-2017.
 *
 * Seam-carving is a content-aware image resizing technique where
 * the image is reduced in size by one pixel of height (or width) at a time.
 * A vertical seam in an image is a path of pixels connected from the top to the bottom with one pixel in each row.
 * (A horizontal seam is a path of pixels connected from the left to the right with one pixel in each column.)
 *
 * SeamCarver is a data type that resizes a W-by-H image using the seam-carving technique.
 *
 */

public class SeamCarver {

    private Picture picture;
    private double[][] energyMatrix;
    private boolean isTransposed; // false by default
    private boolean calledFromHorizontal; // false by default

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new NullPointerException();
        }
        this.picture = new Picture(picture);
        this.energyMatrix = new double[picture.width()][picture.height()];
        calculateEnergyMatrix();
    }

    // current picture
    public Picture picture() {
        if (isTransposed) { // make sure the orientation is correct
            transposePicture();
            isTransposed = false;
        }
        return picture;
    }

    // width of current picture
    public int width() {
        return picture.width();
    }

    // height of current picture
    public int height() {
        return picture.height();
    }

    /**
     * energy of each pixel is a measure of the importance of each pixel—the higher the energy,
     * the less likely that the pixel will be included as part of a seam (as we'll see in the next step).
     * Uses dual-gradient energy function
     * @param x: column of a pixel
     * @param y: row of a pixel
     * @return energy of a pixel
     */
    public double energy(int x, int y) {
        if (x < 0 || y < 0 || x >= picture.width() || y >= picture.height())
            throw new IndexOutOfBoundsException();
        // define the energy of a pixel at the border of the image to be 1000
        if (x == 0 || y == 0 || x == width() - 1 || y == height() - 1) {
            return 1000;
        }
        return Math.sqrt(xGradient(x, y) + yGradient(x, y));
    }

    /**
     * the square of the x-gradient
     * @param x: column of a pixel
     * @param y: row of a pixel
     * @return value of the square of the x-gradient
     */
    private double xGradient(int x, int y) {
        Color rightPixel = picture.get(x + 1, y);
        Color leftPixel = picture.get(x - 1, y);
        return Math.pow(rightPixel.getRed() - leftPixel.getRed(), 2) +
                Math.pow(rightPixel.getGreen() - leftPixel.getGreen(), 2) +
                Math.pow(rightPixel.getBlue() - leftPixel.getBlue(), 2);
    }

    /**
     * the square of the y-gradient
     * @param x: column of a pixel
     * @param y: row of a pixel
     * @return value of the square of the y-gradient
     */
    private double yGradient(int x, int y) {
        Color topPixel = picture.get(x, y - 1);
        Color bottomPixel = picture.get(x, y + 1);
        return Math.pow(topPixel.getRed() - bottomPixel.getRed(), 2) +
                Math.pow(topPixel.getGreen() - bottomPixel.getGreen(), 2) +
                Math.pow(topPixel.getBlue() - bottomPixel.getBlue(), 2);
    }

    /**
     * Calculate energy of each pixel of a picture and save it in a 2d array
     */
    private void calculateEnergyMatrix() {
        for (int row = 0; row < picture.height(); row++) {
            for (int col = 0; col < picture.width(); col++) {
                energyMatrix[col][row] = energy(col, row);
            }
        }
    }

    /**
     * Print energy of each pixel as last calculated by SeamCarver calculateEnergyMatrix().
     */
    private void printEnergyMatrix() {
        System.out.printf("image is %d pixels wide by %d pixels high.\n", picture.width(), picture.height());
        System.out.printf("Printing energy calculated for each pixel.\n");
        for (int row = 0; row < picture.height(); row++) {
            for (int col = 0; col < picture.width(); col++) {
                System.out.printf("%9.0f ", energyMatrix[col][row]);
            }
            System.out.println();
        }
    }

    /**
     * Find a vertical seam of minimum total energy.
     * Sequence of indices for vertical seam.
     * Strategy: go from top to bottom calculating minimal distances to each entry row by row keeping track
     * of the closest path in minPathAncestorsMatrix.
     * Detect a bottom row entry with minimal distance and backtrack using minPathAncestorsMatrix to receive a vertical
     * seam array.
     * Runs in time proportional to width × height in the worst case.
     * @return an array of length H such that entry
     * y is the column number of the pixel to be removed from row y of the image.
     */
    public int[] findVerticalSeam() {
        // reverse to the initial state when transposed and called not from findHorizontalSeam().
        if (isTransposed && !calledFromHorizontal) {
            transposePicture();
            isTransposed = false;
        }
        int[][] minPathAncestorsMatrix = new int[picture.width()][picture.height()];
        int[] seamEntries = new int[height()]; // store entries to be removed from the the image
        double[] currentRowDistances = new double[width()]; // temporarily store minimal distances to each entry of the row processed
        double[] prevRowDistances = new double[width()]; // keep distances to each entry of the last processed row saved

        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                calcMinDistance(x, y, currentRowDistances, prevRowDistances, minPathAncestorsMatrix);
            }
            System.arraycopy(currentRowDistances, 0, prevRowDistances, 0, width());
        }

        int minDistanceEntryCol = getMinEntry(prevRowDistances);
        // backtrack shortest path and get seam entries
        seamEntries[seamEntries.length - 1] = minDistanceEntryCol; // insert last entry into seam.
        for (int i = seamEntries.length - 2; i >= 0; i--) {
            seamEntries[i] = minPathAncestorsMatrix[minDistanceEntryCol][i + 1]; // insert ith entry into seam.
            minDistanceEntryCol = minPathAncestorsMatrix[minDistanceEntryCol][i + 1]; // go one row up
        }
        return seamEntries;
    }

    /**
     * Same as findVerticalSeam()
     * Transpose only if not transposed already.
     * Don't explicitly transpose the Picture until you need to do so.
     * For example, if you perform a sequence of 50 consecutive horizontal seam removals,
     * you should need only two transposes (not 100).
     */
    public int[] findHorizontalSeam() {
        if(!isTransposed) {
            transposePicture();
            isTransposed = true;
        }
        calledFromHorizontal = true;
        int[] seam = findVerticalSeam();
        calledFromHorizontal = false; // a trick that allows not to transpose until it's required.
        return seam;
    }

    /**
     * Helper method.
     * Calculates a minimal distance to pixels of each row one at a time.
     * @param x: column
     * @param y: row
     * @param currentRowDistances: min distances to the entries of the current row
     * @param prevRowDistances: min distances to the entries of the above row
     */
    private void calcMinDistance(int x, int y, double[] currentRowDistances, double[] prevRowDistances, int[][] minPathAncestorsMatrix) {
        // case 1: top row. Distance to each entry is 1000 by default. Entries don't have ancestors.
        if (y == 0) {
            minPathAncestorsMatrix[x][y] = -1; // set min path parent of an entry (x, y)
            currentRowDistances[x] = 1000; // set distance to an entry (x, y)
        }
        // case 2: Left-most column. Entry has 2 ancestors at positions (x, y-1) and (x+1, y-1)
        else if (x == 0) {
            double top = prevRowDistances[x];
            double topRight = prevRowDistances[x + 1];
            if (top < topRight) {
                minPathAncestorsMatrix[x][y] = x;  // set min path parent of an entry (x, y)
                currentRowDistances[x] = top + energyMatrix[x][y]; // set distance to an entry (x, y)
                // (distance to a min math parent + distance to an entry from parent
            } else {
                minPathAncestorsMatrix[x][y] = x + 1;
                currentRowDistances[x] = topRight + energyMatrix[x][y];
            }
        }
        // case 3: Right-most column. Entry has 2 ancestors at positions (x-1, y-1) and (x, y-1)
        else if (x == width() - 1) {
            double top = prevRowDistances[x];
            double topLeft = prevRowDistances[x - 1];
            if (top < topLeft) {
                minPathAncestorsMatrix[x][y] = x;
                currentRowDistances[x] = top + energyMatrix[x][y];
            } else {
                minPathAncestorsMatrix[x][y] = x - 1;
                currentRowDistances[x] = topLeft + energyMatrix[x][y];
            }
        }
        // case 4: Entry has 3 ancestors at positions (x-1, y-1), (x, y-1), (x+1, y-1)
        else {
            double topLeft = prevRowDistances[x - 1];
            double top = prevRowDistances[x];
            double topRight = prevRowDistances[x + 1];
            double min = Math.min(Math.min(topLeft, top), topRight);
            if (min == topLeft) {
                minPathAncestorsMatrix[x][y] = x - 1;
            } else if (min == top) {
                minPathAncestorsMatrix[x][y] = x;
            } else {
                minPathAncestorsMatrix[x][y] = x + 1;
            }
            currentRowDistances[x] = min + energyMatrix[x][y];
        }
    }

    /**
     * Helper method.
     * @param distances: array of distances
     * @return a column of an entry with a minimal distance in a passed row of distances
     */
    private int getMinEntry(double[] distances) {
        int minDistanceEntryCol = 0;
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] < distances[minDistanceEntryCol]) {
                minDistanceEntryCol = i;
            }
        }
        return minDistanceEntryCol;
    }

    /**
     * remove vertical seam from current picture
     * @param seam sequence of entries for deletion
     */
    public void removeVerticalSeam(int[] seam) {
        // Throw a java.lang.NullPointerException if removeVerticalSeam() is called with a null argument.
        if (seam == null) {
            throw new NullPointerException();
        }

        if (isTransposed && !calledFromHorizontal) {
            transposePicture();
            isTransposed = false;
        }

        if (!isValidSeam(seam) || width() <= 1) {
            throw new IllegalArgumentException();
        }
        Picture newPicture = new Picture(width() - 1, height());
        for (int y = 0; y < height(); y++) { // iterate through rows
            int i = 0;
            for (int x = 0; x < width(); x++) { // iterate through columns
                if (x != seam[y]) { // avoid entry to be deleted
                   newPicture.set(i, y, picture.get(x, y));
                   i += 1;
                }
            }
        }
        picture = newPicture;
        energyMatrix = new double[width()][height()]; // reset the energy matrix
        calculateEnergyMatrix();
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if(!isTransposed) {
            transposePicture();
            isTransposed = true;
        }
        calledFromHorizontal = true;
        removeVerticalSeam(seam);
        calledFromHorizontal = false; // a trick that allows not to transpose until it's required.
    }

    /**
     * Helper method
     * Throw a java.lang.IllegalArgumentException if removeVerticalSeam() is called with an array of the wrong length
     * or if the array is not a valid seam
     * (i.e., either an entry is outside its prescribed range or two adjacent entries differ by more than 1).
     * @param seam sequence of entries for deletion
     * @return true if seam is valid. false otherwise.
     */
    private boolean isValidSeam(int [] seam) {
        if (seam.length != height()) {
            return false;
        }
        for (int i = 0; i < seam.length; i++) {
            int entry = seam[i];
            if (entry < 0 || entry > width() - 1) {
                return false;
            }
            if (i != seam.length - 1) {
                if (Math.abs(entry - seam[i + 1]) > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void transposePicture() {
        Picture transposedPicture = new Picture(picture.height(), picture.width());
        double[][] transposedEnergyMatrix = new double[transposedPicture.width()][transposedPicture.height()];
        for(int x = 0; x < picture.width(); x++) {
            for(int y = 0; y < picture.height(); y++) {
                transposedPicture.set(y, x, picture.get(x, y));
                transposedEnergyMatrix[y][x] = energyMatrix[x][y];
            }
        }
        // reset picture and energyMatrix to a transposed version
        picture = transposedPicture;
        energyMatrix = transposedEnergyMatrix;
    }

    public static void main(String[] args) {
        Picture picture = new Picture("10x12.png");
        SeamCarver seamCarver = new SeamCarver(picture);
        seamCarver.calculateEnergyMatrix();
        seamCarver.printEnergyMatrix();

        int[] seamV = seamCarver.findVerticalSeam();
        System.out.println("Vertical seam: ");
        for(Integer col : seamV) {
            System.out.printf(col + " ");
        }

        System.out.println();

        System.out.println("Horizontal seam: ");
        int[] seamH = seamCarver.findHorizontalSeam();
        for(Integer col : seamH) {
            System.out.printf(col + " ");
        }

        System.out.println();

    }
}
