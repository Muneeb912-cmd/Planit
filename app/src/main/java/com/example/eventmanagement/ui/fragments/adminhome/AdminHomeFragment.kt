package com.example.eventmanagement.ui.fragments.adminhome

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentAdminHomeBinding
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.bottomsheets.addeditevent.AddEditEventFragment
import com.example.eventmanagement.ui.bottomsheets.eventdetails.EventDetailsFragment
import com.example.eventmanagement.ui.sharedviewmodel.SharedViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale


class AdminHomeFragment : Fragment(), AdminHomeAdapter.OnItemClickListener {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentAdminHomeBinding
    private var selectedDate: LocalDate? = null
    private var isEventDetailsBottomSheetShown = false

    private val eventDates: Set<LocalDate>
        @RequiresApi(Build.VERSION_CODES.O)
        get() = sharedViewModel.allEvents.value
            .mapNotNull { it.eventDate }
            .flatMap { parseEventDates(it) }
            .toSet()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendarView()
        setInitialSelection()
        setUpAdapter()
        observeEvents()
        binding.monthsContainer.prevMonthButton.setOnClickListener { navigateToPreviousMonth() }
        initializeFab()
        binding.monthsContainer.nextMonthButton.setOnClickListener { navigateToNextMonth() }

    }

    private fun initializeFab() {
        binding.addEventFab.setOnClickListener { showAddEventBottomSheet() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeEvents() {
        lifecycleScope.launch {
            launch {
                sharedViewModel.allEvents.collect {
                    setupEventsList()
                    setupCalendarView()
                }
            }
        }
    }

    private fun showAddEventBottomSheet() {
        binding.addEventFab.isEnabled = false
        val bottomSheetFragment = AddEditEventFragment()
        val bundle = Bundle().apply {
            putString("key", "create")
            putSerializable("eventData", null)
        }
        bottomSheetFragment.arguments = bundle
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        bottomSheetFragment.setOnDismissListener {
            binding.addEventFab.isEnabled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupEventsList() {
        val selectedDateString =
            selectedDate?.toString()
        Log.d("HomeFragment", "Selected Date: $selectedDateString")

        val filteredEvents = sharedViewModel.allEvents.value.filter { event ->
            event.isEventPublic == true && (
                    event.eventDate == selectedDateString ||
                            isDateWithinEventDateRange(selectedDateString, event.eventDate)
                    )
        }

        if (filteredEvents.isNotEmpty()) {
            binding.eventsList.visibility = View.VISIBLE
            binding.noEventsFoundText.visibility = View.GONE
            (binding.eventsList.adapter as? AdminHomeAdapter)?.updatedFilteredEvents(
                filteredEvents
            )


        } else {
            binding.eventsList.visibility = View.GONE
            binding.noEventsFoundText.visibility = View.VISIBLE
        }
    }

    private fun setUpAdapter() {
        val adapter =
            AdminHomeAdapter(emptyList(), this)
        binding.eventsList.layoutManager = LinearLayoutManager(context)
        binding.eventsList.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDateWithinEventDateRange(selectedDate: String?, eventDate: String?): Boolean {
        if (selectedDate == null || eventDate == null) {
            return false
        }
        return if (eventDate.contains(" - ")) {
            val dates = eventDate.split(" - ")
            if (dates.size == 2) {
                val startDate = dates[0]
                val endDate = dates[1]
                isDateWithinRange(selectedDate, startDate, endDate)
            } else {
                false
            }
        } else {
            eventDate == selectedDate
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDateWithinRange(
        selectedDate: String?,
        startDate: String?,
        endDate: String?
    ): Boolean {
        if (selectedDate == null || startDate == null || endDate == null) {
            return false
        }
        val selectedLocalDate = LocalDate.parse(selectedDate)
        val startLocalDate = LocalDate.parse(startDate)
        val endLocalDate = LocalDate.parse(endDate)

        return !selectedLocalDate.isBefore(startLocalDate) && !selectedLocalDate.isAfter(
            endLocalDate
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCalendarView() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val daysOfWeek = daysOfWeek()
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

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

        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    if (container.titlesContainer.tag == null) {
                        container.titlesContainer.tag = data.yearMonth
                        container.titlesContainer.children.map { it as TextView }
                            .forEachIndexed { index, textView ->
                                val dayOfWeek = daysOfWeek[index]
                                val title =
                                    dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                textView.text = title
                                textView.setTextColor(requireContext().getColor(android.R.color.white))
                            }
                        val monthYearText = "${
                            data.yearMonth.month.getDisplayName(
                                TextStyle.FULL,
                                Locale.getDefault()
                            )
                        } ${data.yearMonth.year}"
                        binding.monthsContainer.monthYearTextView.text = monthYearText
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setInitialSelection() {
        selectedDate = LocalDate.now()
        selectedDate?.let {
            binding.calendarView.notifyDateChanged(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        val monthYearText = "${
            yearMonth.month.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            )
        } ${yearMonth.year}"
        binding.monthsContainer.monthYearTextView.text = monthYearText
    }

    override fun onItemClick(cardData: EventData) {
        if (!isEventDetailsBottomSheetShown) {
            isEventDetailsBottomSheetShown = true

            val bottomSheetFragment = EventDetailsFragment(cardData)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

            bottomSheetFragment.setOnDismissListener {
                isEventDetailsBottomSheetShown = false
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseEventDates(eventDate: String): List<LocalDate> {
        val dateRange = eventDate.split(" - ")

        return if (dateRange.size == 2) {
            try {
                val startDate = LocalDate.parse(dateRange[0], dateFormatter)
                val endDate = LocalDate.parse(dateRange[1], dateFormatter)
                generateDateRange(startDate, endDate)
            } catch (e: DateTimeParseException) {
                Log.e("HomeFragment", "Date parsing error: ${e.message}")
                emptyList()
            }
        } else {
            try {
                listOf(LocalDate.parse(eventDate, dateFormatter))
            } catch (e: DateTimeParseException) {
                Log.e("HomeFragment", "Date parsing error: ${e.message}")
                emptyList()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        return dates
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val dayTextView: TextView = view.findViewById(R.id.dayTextView)
        val eventDot: View = view.findViewById(R.id.eventDot)
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }

}