package com.example.data.repository

import com.example.R
import com.example.data.model.*

class KrishiRepository {

    fun getMandiRates(): List<MandiCropRate> {
        return listOf(
            MandiCropRate(
                id = "1",
                commodity = "Tomato",
                variety = "Hybrid",
                state = "Andhra Pradesh",
                district = "Kolar Region",
                market = "Kolar Market, AP",
                modalPrice = 1620.0,
                minPrice = 1400.0,
                maxPrice = 1850.0,
                updateTime = "10:30 AM",
                arrivalQty = "450 qtl",
                imageResId = R.drawable.crop_tomato_1785933383186
            ),
            MandiCropRate(
                id = "2",
                commodity = "Maize",
                variety = "Yellow",
                state = "Andhra Pradesh",
                district = "Eluru",
                market = "Eluru Market, AP",
                modalPrice = 2080.0,
                minPrice = 1950.0,
                maxPrice = 2200.0,
                updateTime = "10:30 AM",
                arrivalQty = "890 qtl",
                imageResId = R.drawable.crop_maize_1785933397052
            ),
            MandiCropRate(
                id = "3",
                commodity = "Paddy",
                variety = "BPT 5204",
                state = "Andhra Pradesh",
                district = "East Godavari",
                market = "Razole Market, AP",
                modalPrice = 2340.0,
                minPrice = 2200.0,
                maxPrice = 2450.0,
                updateTime = "10:30 AM",
                arrivalQty = "1250 qtl",
                imageResId = R.drawable.crop_paddy_1785933411309
            ),
            MandiCropRate(
                id = "4",
                commodity = "Chilli",
                variety = "Teja Red",
                state = "Andhra Pradesh",
                district = "Guntur",
                market = "Guntur Market, AP",
                modalPrice = 11500.0,
                minPrice = 10200.0,
                maxPrice = 12800.0,
                updateTime = "10:30 AM",
                arrivalQty = "620 qtl",
                imageResId = R.drawable.crop_chilli_1785933423205
            ),
            MandiCropRate(
                id = "5",
                commodity = "Cotton",
                variety = "Medium Staple",
                state = "Telangana",
                district = "Warangal",
                market = "Warangal APMC",
                modalPrice = 7200.0,
                minPrice = 6800.0,
                maxPrice = 7600.0,
                updateTime = "10:15 AM",
                arrivalQty = "780 qtl"
            ),
            MandiCropRate(
                id = "6",
                commodity = "Onion",
                variety = "Red Nasik",
                state = "Maharashtra",
                district = "Nashik",
                market = "Lasalgaon Market",
                modalPrice = 1950.0,
                minPrice = 1500.0,
                maxPrice = 2300.0,
                updateTime = "10:00 AM",
                arrivalQty = "3200 qtl"
            )
        )
    }

    fun getFarmTools(): List<FarmToolItem> {
        return listOf(
            FarmToolItem("1", "Tractor Calculator", ToolCategory.TRACTOR, "tractor", "Tractor diesel & per acre plowing work cost"),
            FarmToolItem("2", "Bags → ₹ Calculator", ToolCategory.SALE, "bag", "Calculate total earnings from bags to rupees"),
            FarmToolItem("3", "Profit / Loss Calculator", ToolCategory.FINANCIAL, "chart", "Estimate gross return and net profit per crop"),
            FarmToolItem("4", "Labour Calculator", ToolCategory.LABOUR, "labour", "Daily wage distribution for field workers"),
            FarmToolItem("5", "Interest Calculator", ToolCategory.FINANCIAL, "interest", "Simple & Compound village loan interest calculator"),
            FarmToolItem("6", "EMI Calculator", ToolCategory.FINANCIAL, "emi", "KCC loan and tractor finance EMI planner"),
            FarmToolItem("7", "SIP Calculator", ToolCategory.FINANCIAL, "sip", "Farmer wealth growth & savings calculator"),
            FarmToolItem("8", "My Book", ToolCategory.FINANCIAL, "book", "Digital khata to track farm expenses & income"),
            FarmToolItem("9", "Irrigation Calculator", ToolCategory.IRRIGATION, "pump", "Water pump run-time & electricity cost"),
            FarmToolItem("10", "All Tools", ToolCategory.ALL, "grid", "Explore full suite of 15+ agricultural tools")
        )
    }

    fun getInitialMoneyBook(): List<MoneyBookEntry> {
        return listOf(
            MoneyBookEntry("1", "Paddy Yield Sale", 45000.0, true, "Crop Sale", "02 Aug 2026", "Sold 20 Quintals BPT Rice"),
            MoneyBookEntry("2", "DAP Fertilizer 5 Bags", 6750.0, false, "Fertilizers", "01 Aug 2026", "Purchased from Agri Kendra"),
            MoneyBookEntry("3", "Tractor Plowing Wages", 3200.0, false, "Machinery", "28 Jul 2026", "4 hours rotavator work"),
            MoneyBookEntry("4", "Tomato Sale (Local APMC)", 18400.0, true, "Crop Sale", "25 Jul 2026", "Direct market dispatch")
        )
    }

    fun getCropListings(): List<CropListing> {
        return listOf(
            CropListing("1", "M. Subba Rao", "Eluru, AP", "Organic Paddy (BPT 5204)", 50, 2350.0, "+91 98480 12345", "05 Aug 2026"),
            CropListing("2", "K. Venkateswarlu", "West Godavari, AP", "Chilli (Teja Variety)", 15, 11800.0, "+91 94401 67890", "04 Aug 2026"),
            CropListing("3", "Sita Devi", "Krishna District, AP", "Fresh Hybrid Tomato", 30, 1650.0, "+91 91234 56789", "04 Aug 2026")
        )
    }

    fun getFarmerStories(): List<FarmerStory> {
        return listOf(
            FarmerStory("1", "Koteswara Rao", "Eluru, Andhra Pradesh", "Drip Irrigation Doubled My Maize Profits", "By switching to sub-surface drip irrigation and soil testing, I reduced water usage by 40% and harvested 38 bags/acre!"),
            FarmerStory("2", "Anusuya Devi", "Guntur, AP", "Natural Chilli Farming with Zero Chemical Pesticides", "Using Neem oil sprays and vermicompost, my chilli pods earned premium export market prices in Guntur Yard.")
        )
    }

    fun getAgriNews(): List<AgriNewsItem> {
        return listOf(
            AgriNewsItem("1", "PM-KISAN 17th Installment Date Announced for Farmers", "05 Aug 2026", "Govt Scheme", "Government releases ₹2,000 directly to verified farmer bank accounts across Andhra Pradesh & Telangana."),
            AgriNewsItem("2", "AP Government Sets Minimum Support Price for Kharif Paddy", "04 Aug 2026", "Mandi Updates", "State procurement centers to start purchasing Grade A paddy at ₹2,300/qtl with instant bank transfer.")
        )
    }
}
