package com.example.javatravel.controller;

import com.example.javatravel.dto.FinalPriceDto;
import com.example.javatravel.dto.FinalPurchaseDto;
import com.example.javatravel.dto.PurchaseDto;
import com.example.javatravel.entity.PriceEntity;
import com.example.javatravel.entity.TripEntity;
import com.example.javatravel.entity.enums.StandardType;
import com.example.javatravel.service.PurchaseService;
import com.example.javatravel.service.TripService;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Controller
@AllArgsConstructor
public class NewPurchaseController {

    private PurchaseService purchaseService;
    private TripService tripService;


    @GetMapping("/purchase")
    public String getPurchase(@RequestParam("selectedTripId") Long selectedTripId, Model model) {
        TripEntity trip = tripService.getTripById(selectedTripId);
        model.addAttribute("selectedTrip", trip);
        model.addAttribute("standards", StandardType.values());
        return "purchase";
    }


    public BigDecimal getFinalPrice(Long selectedTripId,
                                    Integer adultNumber,
                                    Integer childNumber,
                                    StandardType standardType) {
        //StandardType standard = StandardType.OB;
//         StandardType selectedStandard = standardService.getStandardByCode(standard).getStandardType();

        TripEntity selectedTrip = tripService.getTripById(selectedTripId);

        PriceEntity priceEntity = selectedTrip.getPrice();

        BigDecimal finalPrice = BigDecimal.ZERO;
        double priceByStandard = 0.0;
        if (standardType.equals(StandardType.BB)) {
            priceByStandard = 1.05;
        }
        if (standardType.equals(StandardType.HB)) {
            priceByStandard = 1.1;
        }
        if (standardType.equals(StandardType.FB)) {
            priceByStandard = 1.15;
        }
        if (standardType.equals(StandardType.AI)) {
            priceByStandard = 1.20;
        }

        LocalDate startDate = selectedTrip.getStartDate();
        LocalDate endDate = selectedTrip.getEndDate();
        Period period = Period.between(startDate, endDate);
        int tripDuration = period.getDays();

        finalPrice = finalPrice.add(BigDecimal.valueOf(priceEntity.getFlightPrice() * (adultNumber + childNumber)))
                .add(BigDecimal.valueOf(priceEntity.getPricePerDay() * adultNumber * priceByStandard * tripDuration))
                .add(BigDecimal.valueOf(priceEntity.getPricePerDay() * childNumber * priceByStandard * tripDuration / 2));

        return finalPrice;

//        return
//                finalPrice.add(BigDecimal.valueOf(priceEntity.getFlightPrice() * (adultNumber + childNumber)))
//                        .add(BigDecimal.valueOf(priceEntity.getPricePerDay() * adultNumber))
//                        .add(BigDecimal.valueOf(priceEntity.getPricePerDay() * childNumber / 2));

    }

    @PostMapping("/purchase")
    public String displayPrice(@ModelAttribute("finalPrice") FinalPriceDto dto,
                               Model model) {

        BigDecimal finalPrice = getFinalPrice(dto.getSelectedTripId(),
                dto.getAdultNumber(),
                dto.getChildNumber(),
                StandardType.valueOf(dto.getStandard()));
        model.addAttribute("finalPrice", finalPrice);
        model.addAttribute("selectedTrip", tripService.getTripById(dto.getSelectedTripId()));
        model.addAttribute("adultNumber",dto.getAdultNumber());
        model.addAttribute("childNumber",dto.getChildNumber());
        model.addAttribute("standard",dto.getStandard());

        return "finalPrice";
    }

    @GetMapping("/final_price")
    public String getPurchaseDetails(@RequestParam("selectedTripId") Long selectedTripId, Model model,
                                     @RequestParam("adultNumber") Integer adultNumber,
                                     @RequestParam("childNumber") Integer childNumber,
                                     @RequestParam("selectedStandard") StandardType selectedStandard,
                                     @RequestParam("finalPrice") BigDecimal finalPrice) {
        TripEntity trip = tripService.getTripById(selectedTripId);
        model.addAttribute("selectedTrip", trip);

        model.addAttribute("selectedStandard", StandardType.values());
        model.addAttribute("adultNumber", adultNumber);
        model.addAttribute("childNumber", childNumber);
        model.addAttribute("finalPrice", finalPrice);

        return "finalPrice";
    }

    //przez model atribute
    @PostMapping("/final_price")
    public String createPurchase(@ModelAttribute ("finalPurchase") PurchaseDto dto,
                                 Model model) {

        TripEntity selectedTrip =tripService.getTripById(dto.getTripId());
        int adultNumber = dto.getAdultNumber();
        int childNumber = dto.getChildNumber();

//        StandardEntity selectedStandard = standardService.getStandardByStandardType(standardType);

        String email = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

//        if (selectedTrip != null) {
//            PurchaseDto newPurchaseDto = new PurchaseDto();
//            newPurchaseDto.setTrip(selectedTrip);
//            newPurchaseDto.setAdultNumber(adultNumber);
//            newPurchaseDto.setChildNumber(childNumber);

          purchaseService.createPurchase(dto,email);



//        }
        return "confirmation";
    }
}
