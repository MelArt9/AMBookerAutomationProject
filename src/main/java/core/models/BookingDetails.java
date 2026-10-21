package core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BookingDetails {

    private final String firstname;
    private final String lastname;
    private final int totalprice;
    private final boolean depositpaid;
    private final BookingDates bookingdates;
    private final String additionalneeds;

    @JsonCreator
    public BookingDetails(
            @JsonProperty("firstname") String firstname,
            @JsonProperty("lastname") String lastname,
            @JsonProperty("totalprice") int totalprice,
            @JsonProperty("depositpaid") boolean depositpaid,
            @JsonProperty("bookingdates") BookingDates bookingdates,
            @JsonProperty("additionalneeds") String additionalneeds) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.totalprice = totalprice;
        this.depositpaid = depositpaid;
        this.bookingdates = bookingdates;
        this.additionalneeds = additionalneeds;
    }

    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public int getTotalprice() { return totalprice; }
    public boolean isDepositpaid() { return depositpaid; }
    public BookingDates getBookingdates() { return bookingdates; }
    public String getAdditionalneeds() { return additionalneeds; }

    // вложенный класс для дат
    public static class BookingDates {

        private final String checkin;
        private final String checkout;

        @JsonCreator
        public BookingDates(
                @JsonProperty("checkin") String checkin,
                @JsonProperty("checkout") String checkout) {
            this.checkin = checkin;
            this.checkout = checkout;
        }

        public String getCheckin() { return checkin; }
        public String getCheckout() { return checkout; }
    }
}