package com.travelmaster.booking.mapper;

import com.travelmaster.booking.dto.BookingResponse;
import com.travelmaster.booking.dto.CreateBookingRequest;
import com.travelmaster.booking.dto.PassengerResponse;
import com.travelmaster.booking.entity.Booking;
import com.travelmaster.booking.entity.Passenger;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingReference", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "numberOfPassengers", expression = "java(request.getPassengers().size())")
    @Mapping(target = "passengers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Booking toEntity(CreateBookingRequest request);

    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    @Mapping(target = "passengers", source = "passengers")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "dateOfBirth", expression = "java(parseDate(request.getDateOfBirth()))")
    @Mapping(target = "passportExpiry", expression = "java(parseDate(request.getPassportExpiry()))")
    Passenger toPassengerEntity(CreateBookingRequest.PassengerRequest request);

    @Mapping(target = "gender", expression = "java(passenger.getGender() != null ? passenger.getGender().name() : null)")
    @Mapping(target = "age", expression = "java(passenger.getAge())")
    PassengerResponse toPassengerResponse(Passenger passenger);

    default LocalDate parseDate(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        return LocalDate.parse(date, DATE_FORMATTER);
    }
}

