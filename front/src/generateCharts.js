const labels = {
    temperature: {
        en: "Temperature (°C)",
        ru: "Температура (°C)",
        kz: "Температура (°C)"
    },
    pressure: {
        en: "Pressure (kg/cm^2)",
        ru: "Давление (кг/см^2)",
        kz: "Қысым (кг/см^2)"
    },
    commercial_performance: {
        en: "Commercial Performance (m^3/hour)",
        ru: "Коммерческая производительность (м^3/час)",
        kz: "Коммерциялық өнімділік (м^3/сағ)"
    }
};

function goToIndex() {
    window.location.href = "index.html";
}

let myChart; // Объявляем переменную для хранения экземпляра графика
function generateChart(data) {
    const xValues = [];
    const yValues = [];
    const barColors = ["red", "green", "blue", "orange", "brown"];

    data.result.forEach(item => {
        var certainValue = item.certainValue || 0;
        xValues.push(certainValue);
        yValues.push(certainValue);
    });
    const selectedLabel = document.getElementById("labelSelect").value;
    const selectedLanguage = document.getElementById("languageSelect").value || "en";
    const label = labels[selectedLabel][selectedLanguage] || "";

    const chartData = {
        labels: xValues,
        datasets: [{
            backgroundColor: barColors,
            data: yValues
        }]
    };

    const chartOptions = {
        legend: { display: false },
        title: {
            display: true,
            text: label
        }
    };
    if (myChart) {
        myChart.destroy();
    }

    var ctx = document.getElementById("myChart").getContext("2d");
    myChart = new Chart(ctx, {
        type: "bar",
        data: chartData,
        options: chartOptions
    });
}

function fetchDataAndGenerateChart() {
    const labelSelect = document.getElementById("labelSelect");
    const selectedLabel = labelSelect.value;
    const validLabels = ['temperature', 'pressure', 'commercial_performance'];

    // Check if the provided label is valid
    if (!validLabels.includes(selectedLabel)) {
        console.error(`Invalid label: ${selectedLabel}`);
        return;
    }

    fetch(`http://localhost:8080/api/v1/results?label=${selectedLabel}`)
        .then(response => response.json())
        .then(data => {
            console.log("Received data:", data);
            generateChart(data, selectedLabel);
        })
        .catch(error => {
            console.error("Error:", error);
        });
}
