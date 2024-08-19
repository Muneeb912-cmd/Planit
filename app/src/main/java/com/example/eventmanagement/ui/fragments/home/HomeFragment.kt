package com.example.eventmanagement.ui.fragments.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.HomeEventCardAdapter
import com.example.eventmanagement.databinding.FragmentHomeBinding
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details.EventDetailsFragment
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class HomeFragment : Fragment(),HomeEventCardAdapter.OnItemClickListener {

    private lateinit var binding: FragmentHomeBinding
    private var selectedDate: LocalDate? = null
    private val eventCardList = listOf(
        EventData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-10",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "On-Going"
        ),
        EventData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-14",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "On-Going"
        ),
        EventData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-20",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Up-Coming"
        ),
        EventData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-21",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Up-Coming"
        ),
        EventData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-20",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Up-Coming"
        ),
        EventData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-15",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Missed"
        ),
        EventData(
            eventTitle = "Popular Event 1",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-01",
            isEventFeatured = false,
            isEventPopular = true,
            isEventPublic = true,
            numberOfPeopleAttending = 28,
            eventStatus = "Missed"
        ),
        EventData(
            eventTitle = "Popular Event 2",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-14",
            isEventFeatured = false,
            isEventPopular = true,
            isEventPublic = true,
            numberOfPeopleAttending = 14,
            eventStatus = "Missed"
        ),
    )
    private val eventDates: Set<LocalDate>
        @RequiresApi(Build.VERSION_CODES.O)
        get() = eventCardList.mapNotNull {
            LocalDate.parse(it.eventDate)
        }.toSet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendarView()
        setInitialSelection()
        binding.monthsContainer.prevMonthButton.setOnClickListener {
            navigateToPreviousMonth()
        }

        binding.monthsContainer.nextMonthButton.setOnClickListener {
            navigateToNextMonth()
        }

        setupEventsList()
    }

    private fun setupEventsList() {
        val selectedDateString = selectedDate?.toString()
        Log.d("HomeFragment", "Selected Date: $selectedDateString")

        val filteredEvents = eventCardList.filter {
            it.isEventPublic == true && it.eventDate == selectedDateString
        }

        if (filteredEvents.isNotEmpty()) {
            binding.eventsList.visibility = View.VISIBLE
            binding.noEventsFoundText.visibility = View.GONE
            val adapter = HomeEventCardAdapter(filteredEvents, this)
            binding.eventsList.layoutManager = LinearLayoutManager(context)
            binding.eventsList.adapter = adapter
            adapter.notifyDataSetChanged()

        } else {
            binding.eventsList.visibility = View.GONE
            binding.noEventsFoundText.visibility = View.VISIBLE
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendarView() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val daysOfWeek = daysOfWeek()
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        // Set up day binder
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.dayTextView.text = data.date.dayOfMonth.toString()
                container.dayTextView.setOnClickListener {
                    handleDayClick(data)
                }
                if (data.position == DayPosition.MonthDate) {
                    container.dayTextView.visibility = View.VISIBLE
                    if (data.date == selectedDate) {
                        container.dayTextView.setTextColor(requireContext().getColor(android.R.color.white))
                        container.dayTextView.setBackgroundResource(R.drawable.indicator_active)
                    } else {
                        container.dayTextView.setTextColor(requireContext().getColor(android.R.color.black))
                        container.dayTextView.background = null
                    }
                    if (data.date in eventDates) {
                        container.eventDot.visibility = View.VISIBLE
                    } else {
                        container.eventDot.visibility = View.GONE
                    }
                } else {
                    container.dayTextView.setTextColor(requireContext().getColor(android.R.color.darker_gray))
                    container.dayTextView.background = null
                    container.eventDot.visibility = View.GONE
                }
            }
        }

        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            textView.text = title
                            textView.setTextColor(requireContext().getColor(android.R.color.white))
                        }
                    val monthYearText = "${data.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${data.yearMonth.year}"
                    binding.monthsContainer.monthYearTextView.text = monthYearText
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setInitialSelection() {
        selectedDate = LocalDate.now() // Set initial selection to today's date
        selectedDate?.let {
            binding.calendarView.notifyDateChanged(it)
        }
    }

    private fun handleDayClick(day: CalendarDay) {
        if (day.position == DayPosition.MonthDate) {
            val currentSelection = selectedDate
            if (currentSelection == day.date) {
                selectedDate = null
                currentSelection.let {
                    binding.calendarView.notifyDateChanged(it)
                }
            } else {
                selectedDate = day.date
                binding.calendarView.notifyDateChanged(day.date)
                currentSelection?.let {
                    binding.calendarView.notifyDateChanged(it)
                }
            }
        }
        setupEventsList()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToPreviousMonth() {
        val currentMonth = binding.calendarView.findFirstVisibleMonth()?.yearMonth ?: return
        val previousMonth = currentMonth.minusMonths(1)
        binding.calendarView.scrollToMonth(previousMonth)
        updateMonthYearText(previousMonth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToNextMonth() {
        val currentMonth = binding.calendarView.findFirstVisibleMonth()?.yearMonth ?: return
        val nextMonth = currentMonth.plusMonths(1)
        binding.calendarView.scrollToMonth(nextMonth)
        updateMonthYearText(nextMonth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMonthYearText(yearMonth: YearMonth) {
        val monthYearText = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}"
        binding.monthsContainer.monthYearTextView.text = monthYearText
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val dayTextView: TextView = view.findViewById(R.id.dayTextView)
        val eventDot: View = view.findViewById(R.id.eventDot)
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }

    override fun onItemClick(cardData: EventData) {
        val bottomSheetFragment = EventDetailsFragment(cardData)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

}
