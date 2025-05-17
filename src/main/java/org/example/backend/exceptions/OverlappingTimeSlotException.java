package org.example.backend.exceptions;

public class OverlappingTimeSlotException extends Exception{
    public OverlappingTimeSlotException(){
        super("Time slot you are trying to add is overlapping with an existing one");
    }

    public OverlappingTimeSlotException(String message){
        super(message);
    }
}
