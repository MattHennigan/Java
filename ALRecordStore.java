// ALRecordStore version 3.4.2
// Written by Matt Hennigan and Jake Owen

// ALRecordStore.java is a file written as part of a pair programming task to create a functional system for a record store.

package warehouse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class ALRecordStore implements RecordMerchant {

	/**
	 * Represents a record in the store. Uniquely identified by an id.
	 */
    static class Record {
        private int num;
        private int reserved;
        private int sold;
        private int soldValue;
        private int price; // When purchasing a record, the system will alert the user that
						   // a price has not been set yet if it is still equal to -1.

        private String artist;
        private String title;
        private String information;
        private String id;
        
        public Record(int num, String artist, String title, String information, String id) {
            this.num = num;
            this.artist = artist;
            this.title = title;
            this.information = information;
            this.id = id;
            this.reserved = 0;
            this.sold = 0;
            this.price = -1;
            this.soldValue = 0;
        }
    }
    
    /**
     * Represents a reservation of a Record.
     */
    static class Reservation {
    	int reservationNum;
    	int numCopies;
    	String recordId;
 
        public Reservation(int reservationNum, int numCopies, String recordId) {
            this.reservationNum = reservationNum;
            this.numCopies = numCopies;
            this.recordId = recordId;
        }
    }
    
	// Values for other methods
	private int totalValueOfRecordsSold = 0;
	private int totalNumRecordsSold = 0;
	private int lastReservationId = 0;
    
    private ObjectArrayList records = new ObjectArrayList();       // Array lists for our records & reservations
    private ObjectArrayList reservations = new ObjectArrayList();
    
    /**
     * Method to create a new reservation number for each customer so it is unique.
     */
    public int getNewReservationId() {
    	lastReservationId += 1;
    	return lastReservationId;
    }
    
    /**
     * Method to search for a record using its id
     */
    public Record getRecordById(String id, boolean requiredToExist) throws IllegalIDException {  
        if (id == null || id.equals("") || id.length() != 8) {
            throw new IllegalIDException();
        }

    	int size = this.records.size();
    	for (int index = 0; index < size; index++) {             // Search through the record array
    		Object recordObject = this.records.get(index);
    		Record record = (Record) recordObject;
    		if (record.id.equals(id)) {
    			return record;
    		}
    	}
    	// Record doesn't exist.
    	if (requiredToExist) {
    		throw new IllegalIDException();
    	} else {
    		return null;    		
    	}
    }

    /**
     * Method adds records to the store with the arguments as record details.
     *
     * @param num               number of Records added
     * @param artist            record artist
     * @param title             record title
     * @param information       free text detailing specific record information
     * @param id                unique ID of record 
     * @throws NegativeNumberOfRecordsAddedException
     * @throws RecordMismatchException
     * @throws IllegalIDException
     */
    public void addRecords(int num, String artist, String title,
                           String information, String id)
        throws NegativeNumberOfRecordsAddedException, RecordMismatchException,
        IllegalIDException {
        if (num < 0) {
            throw new NegativeNumberOfRecordsAddedException();
        }
	
        Record currentRecord = getRecordById(id, false);
        if (currentRecord != null) {
            // Record with same id already exists.
            if (!currentRecord.artist.equals(artist)
                || !currentRecord.title.equals(title)
                || !currentRecord.information.equals(information)) {
                throw new RecordMismatchException();
            }
            
            currentRecord.num += num;
        } else {
            Record newRecord = new Record(num, artist, title, information, id);
            this.records.add(newRecord);
        }
    }

    /**
     * Method adds records to the store with the arguments as record details.
     *
     * @param num               number of Records added
     * @param artist            record artist
     * @param title             record title
     * @param id                unique ID of record 
     * @throws NegativeNumberOfRecordsAddedException
     * @throws RecordMismatchException
     * @throws IllegalIDException
     */
	public void addRecords(int num, String artist, String title, String id) 
        throws NegativeNumberOfRecordsAddedException, RecordMismatchException,
        IllegalIDException {
        
        addRecords(num, artist, title, "", id);    // String information will be added as an empty String
    }
	
    /**
     * Method to set the price of a records with matching ID in stock.
     *
     * @param id                ID of record
     * @param priceInPence      record price in pence
     * @throws NegativePriceException
     * @throws RecordNotInStockException
     * @throws IllegalIDException
     */
	public void setRecordPrice(String id, int priceInPence) throws
    NegativePriceException, RecordNotInStockException, IllegalIDException {
	Record record = getRecordById(id, true);

	int numInStock = record.num - record.reserved;
	if (numInStock > 0) {
		throw new RecordNotInStockException();
	}

		if (priceInPence < 0) {
			throw new NegativePriceException();
		}

		record.price = priceInPence;   // Sets the price of the record
}

	/**
	 * Method sells records with the corresponding ID from the store and removes 
	 * the sold records from the stock.
	 *
	 * @param num           number of records to be sold
	 * @param id            ID of records to be sold
	 * @throws RecordNotInStockException
	 * @throws InsufficientStockException
	 * @throws NegativeNumberOfRecordsSoldException
	 * @throws PriceNotSetException
	 * @throws IllegalIDException
	 */
	public void sellRecords(int num, String id) throws RecordNotInStockException, 
	    InsufficientStockException, NegativeNumberOfRecordsSoldException, 
	    PriceNotSetException, IllegalIDException {
		
		if (num < 0) {
			throw new NegativeNumberOfRecordsSoldException();
		}
		
		Record currentRecord = getRecordById(id, true);
		
		int recordsInStock = currentRecord.num - currentRecord.reserved;
	    if (recordsInStock == 0) {
	    	throw new RecordNotInStockException();
		}
	
	   if (recordsInStock < num) {
		   throw new InsufficientStockException("There are not enough copies of "
		   		+ "this record to reserve, we currently only have" + num
		   		+ "copies of this record available.");
	    }
	
		if (currentRecord.price == -1) {
			throw new PriceNotSetException();
		}
	
		// Add total sale prices.
		int salePrice = (currentRecord.price * num);
		totalValueOfRecordsSold += salePrice;
	
		// Subtract from stock.
		currentRecord.num -= num;
		currentRecord.sold += num;
		currentRecord.soldValue += salePrice;
		
		// Add to total records sold.
		totalNumRecordsSold += num;
	}
    
    /**
     * Method reserves records with the corresponding ID from the store 
     *
     * @param num           number of records to be reserved
     * @param id            ID of records to be reserved
     * @return                  unique reservation number
     * @throws RecordNotInStockException
     * @throws InsufficientStockException
     * @throws NegativeNumberOfRecordsReservedException
     * @throws PriceNotSetException
     * @throws IllegalIDException
     */
    public int reserveRecords(int num, String id) throws RecordNotInStockException, 
        InsufficientStockException, NegativeNumberOfRecordsReservedException, 
        PriceNotSetException, IllegalIDException{
                
        if (id == null || id.equals("")) {
        	throw new IllegalIDException();
        }
 
    	Record record = getRecordById(id, true);
    	
    	int numAvailable = record.num - record.reserved;
    	if (numAvailable < num) {
      		throw new InsufficientStockException("There isn't enough stock, you wanted " + num
      				+ " and there is only " + numAvailable + ".");
    	}
 
    	if (num < 0) {
        	throw new NegativeNumberOfRecordsReservedException();
    	}

    	if (record.price == -1) {                     
      		throw new PriceNotSetException();
    	}
 
    	// num less copies are available when it is reserved.
    	record.reserved += num;

    	// Create and store a new reservation object.
    	Reservation reservation = new Reservation(getNewReservationId(), num, record.id);
    	reservations.add(reservation);
 
    	// Return the unique reservation id.
    	return reservation.reservationNum;
  	}

    /**
     * Method retreives the reservation details when given the reservation number.
     */
    public Reservation getReservation(int reservationNum) throws ReservationNumberNotRecognisedException {
    	for (int index = 0; index < this.reservations.size(); index++) {
    		Object reservationObject = this.reservations.get(index);
    		Reservation reservation = (Reservation) reservationObject;
    		if (reservation.reservationNum == reservationNum) {
    			return reservation;
    		}
    	}
    	throw new ReservationNumberNotRecognisedException();
    }
    
    /**
     * Method removes an existing reservation from the system due to a reservation 
     * cancellation (rather than sale). The stock should therefore remain unchanged.
     *
     * @param reservationNumber           reservation number
     * @throws ReservationNumberNotRecognisedException
     */
    public void unreserveRecords(int reservationNumber)
        throws ReservationNumberNotRecognisedException {
    	Reservation reservation = getReservation(reservationNumber);
    	String recordId = reservation.recordId;
    	Record record;
    	try {
    		record = getRecordById(recordId, true);    		
    	} catch (IllegalIDException e) {
    		// Should not happen, recordId is set by us and checked before the
    		// reservation occurs.
    		return;
    	}
    	record.reserved -= reservation.numCopies;
    }
    
    /**
     * Method sells records with the corresponding reservation number from 
     * the store and removes these sold records from the stock.
     *
     * @param reservationNumber           unique reservation number used to find 
     *                                    record(s) to be sold
     * @throws ReservationNumberNotRecognisedException
     */
    public void sellRecords(int reservationNumber) 
    		throws ReservationNumberNotRecognisedException {
    	Reservation reservation = getReservation(reservationNumber);
    	String recordId = reservation.recordId;
    	Record record;
    	try {
    		record = getRecordById(recordId, true);    		
    	} catch (IllegalIDException e) {
    		// Should not happen, recordId is set by us and checked before the
    		// reservation occurs.
    		return;
    	}
    	record.reserved -= reservation.numCopies;     
    	record.num -= reservation.numCopies;
    }

    /**
     * Access method for the number of records stocked by this RecordMerchant 
     * (reserved and unreserved).
     *
     * @return                  number of records in this store
     */
    public int recordsInStock() {
    	int numInStock = 0;
        for (int index = 0; index < this.records.size(); index++) {
        	Object recordObject = this.records.get(index);
        	Record record = (Record) recordObject;
            numInStock += record.num;
        }
        return numInStock;
    }

    /**
     * Access method for the number of reserved records stocked by this 
     * RecordMerchant.
     *
     * @return                  number of reserved records in this store
     */
    public int reservedRecordsInStock() {
    	int totalRecordsInStock = 0;
		for (int index = 0; index < this.records.size(); index++) {
			Object recordObject = this.records.get(index);
			Record record = (Record) recordObject;
			totalRecordsInStock += record.reserved;
		}
		return totalRecordsInStock;
    }

    /**
     * Method returns number of records with matching ID in stock.
     *
     * @param id            ID of record
     * @return              number of record copies matching ID in stock
     * @throws IllegalIDException
     */
    public int recordsInStock(String id) throws IllegalIDException {
        Record record = getRecordById(id, true);
        return record.num - record.reserved;
    }

    /**
     * Method saves this RecordMerchant's contents into a serialised file, 
     * with the filename given in the argument.
     *
     * @param filename      location of the file to be saved
     * @throws IOException
     */
    public void saveMerchantContents(String filename) throws IOException {
        OutputStream file = new FileOutputStream(filename);
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer);
        try {
            output.writeObject(this);
        } finally {
            output.close();
        }
    }
    
    /**
     * Method to save the system and then close the program.
     */
	public void closeSystemSafely() {
		try {
			saveMerchantContents("backup.txt");			
		} catch (IOException e) {
			System.exit(1);
		}
	    System.exit(0);
	}
	
    /**
     * Method should load and replace this VinylRecordMerchant's contents with the 
     * serialised contents stored in the file given in the argument.
     *
     * @param filename      location of the file to be loaded
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadMerchantContents(String filename) throws IOException, 
        ClassNotFoundException {
        
        InputStream file = new FileInputStream(filename);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);
        try {
            ALRecordStore loadedStore = (ALRecordStore) input.readObject();
            this.records = loadedStore.records;
        } finally {
            input.close();
        }
    }
	
    /**
     * Access method for the number of different records currently stocked by this 
     * RecordMerchant.
     *
     * @return                  number of different specific records currently in 
     *                          this store (i.e. how many different IDs represented
     *                          by records currently in stock)
     */
    public int getNumberOfDifferentRecordsInStock(){
    	int differentRecords = records.size();
    	return differentRecords;
    }

    /**
     * Method to return number of records sold by this RecordMerchant.
     *
     * @return                  number of records sold by the store
     */
    public int getNumberOfSoldRecords(){
    	return totalNumRecordsSold;
	}

    /**
     * Method to return number of records sold by this RecordMerchant with 
     * matching ID.
     *
     * @param id                 ID of records
     * @return                   number records sold by the store with matching ID
     * @throws IllegalIDException
     */
    public int getNumberOfSoldRecords(String id) throws IllegalIDException{
    	Record record = getRecordById(id, true);
    	return record.sold;
	}
  
    /**
     * Method to total price of reserved records in this VinyalRecordMerchant
     * (i.e. income that will be generated if all the reserved stock is sold 
     * to those holding the reservations).
     *
     * @return                  total cost of reserved records
     */ 
    public int getTotalPriceOfReservedRecords(){
    	int totalReservedPrice = 0;
		for (int index = 0; index < this.records.size(); index++) {
			Object recordObject = this.records.get(index);
			Record record = (Record) recordObject;
			// Calculate the price of reserved records
			totalReservedPrice += (record.reserved * record.price);
		}
		return totalReservedPrice;
	}

    /**
     * Method to return total price of records sold by this RecordMerchant
     * (in pence), i.e. income that has been generated by these sales).
     *
     * @return                  total cost of records sold (in pence)
     */
    public int getTotalPriceOfSoldRecords() {
	   return totalValueOfRecordsSold;
    }
   
	/**
	* Method to find total number of vinyl records that have been reserved
	*/
	public int getTotalNumberOfRecordsReserved() {
		int totalReservedRecords = 0;
		for (int index = 0; index < this.records.size(); index++) {
			Object recordObject = this.records.get(index);
			Record record = (Record) recordObject;
			totalReservedRecords += record.reserved;
		}
		return totalReservedRecords;
	}

    /**
     * Method to return total price of records sold by this RecordMerchant
     * (in pence) with  matching ID (i.e. income that has been generated 
     * by these sales).
     *
     * @param id                ID of records
     * @return                  total cost of records sold (in pence) with 
     *                          matching ID
     * @throws IllegalIDException
     */
    public int getTotalPriceOfSoldRecords(String id) throws IllegalIDException{
		Record record = getRecordById(id, true);
		return record.soldValue;
	}

    /**
     * Method to return textual details of a vinyl record in stock. If there 
     * are no String details for a record, there will be an empty String 
     * instance returned.
     *
     * @param id                ID of record
     * @return                  any textual details relating to the record
     * @throws IllegalIDException
     */
    public String getRecordDetails(String id) throws IllegalIDException{
    	Record currentRecord = getRecordById(id, true);
    	return currentRecord.information;
    }

    /**
     * Method empties this VinyalRecordMerchant of its contents and resets 
     * all internal counters.
     */
    public void empty() {
    	this.totalValueOfRecordsSold = 0;
    	this.totalNumRecordsSold =0;
    	this.lastReservationId = 0;
    	this.records = new ObjectArrayList();
    	this.reservations = new ObjectArrayList();
    }

    /**
     * Method resets the tracking of number and costs of all records sold. 
     * The stock levels of this VinyalRecordMerchant and reservations should 
     * be unaffected.
     */
    public void resetSaleAndCostTracking() {
    	this.totalNumRecordsSold = 0;
    	this.totalValueOfRecordsSold = 0;
    	for (int index = 0; index < this.records.size(); index++) {
    		Object recordObject = this.records.get(index);
    		Record record = (Record) recordObject;
    		record.sold = 0;
    		record.soldValue = 0;
    	}
	}
}
