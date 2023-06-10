const form = document.getElementById('dataForm');
const submitButton = document.getElementById('submitButton');

form.addEventListener('submit', event => {
    event.preventDefault();

    const formData = new FormData(form);

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:8080/api/v1/dto', true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    const jsonData = {};
    for (const [key, value] of formData.entries()) {
        jsonData[key] = value;
    }
    console.log('Form Data:', jsonData);

    xhr.send(JSON.stringify(jsonData));

    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log('Data submitted successfully, ', xhr.responseText);
            const resultDto = JSON.parse(xhr.responseText);
            console.log('Result:', resultDto);
            openNewPageWithResult(resultDto);
        } else {
            console.error('Error submitting data');
        }
    };
});

function openNewPageWithResult(resultDto) {
    const result = resultDto.result;

    const sb = [];
    sb.push("<html lang=\"en\">");
    sb.push("<head>");
    sb.push("<title>Result</title>");
    sb.push("<meta charset=\"utf-8\">");
    sb.push("<script src=\"https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.4.0/jspdf.umd.min.js\"></script>")
    sb.push("<style>");
    sb.push("body {");
    sb.push("    font-family: Arial, sans-serif;");
    sb.push("    margin: 20px;");
    sb.push("    background-color: #f7f7f7;");
    sb.push("}");
    sb.push("h1 {");
    sb.push("    text-align: center;");
    sb.push("    color: #333;");
    sb.push("}");
    sb.push("table {");
    sb.push("    margin: 20px auto;");
    sb.push("    border-collapse: collapse;");
    sb.push("    background-color: #fff;");
    sb.push("    box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.1);");
    sb.push("}");
    sb.push("th, td {");
    sb.push("    padding: 12px;");
    sb.push("    border: 1px solid #ddd;");
    sb.push("    text-align: left;");
    sb.push("}");
    sb.push("tr:nth-child(even) {");
    sb.push("    background-color: #f2f2f2;");
    sb.push("}");
    sb.push("</style>");
    sb.push("</head>");
    sb.push("<body>");

    let language = document.getElementById('lang').value;
    console.log('LanguageCode:', language);
    if (language === "ru"){
        sb.push("<h1>Результат</h1>");
        sb.push("<table>");
        sb.push("<tr><th>Описание</th><th>Значение</th></tr>");
        sb.push("<tr><td>Давление газа на выходе нагнетателя</td><td>" + result.gasPressureAtTheBlowerOutlet.toFixed(2) + " (кг/см^2)</td></tr>");
        sb.push("<tr><td>Температура газа на выходе нагнетателя</td><td>" + result.gasTemperatureAtTheBlowerOutlet.toFixed(2) + " (°C)</td></tr>");
        sb.push("<tr><td>Степень сжатия</td><td>" + result.compressionRatio.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Требуемый оборот (искомая частота вращения ротора)</td><td>" + result.requiredRevolutionDesiredRotorSpeed.toFixed(2) + " (м^3/час)</td></tr>");
        sb.push("<tr><td>Коэффициент удаленности от зоны помпажа</td><td>" + result.coefficientOfDistanceFromTheSurgeZone.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Мощность на муфте (Мощность потребляемая нагнетателем)</td><td>" + result.clutchPower.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Располагаемая мощность привода</td><td>" + result.availableDrivePower.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Резерв мощность привода ГПА</td><td>" + result.reserveDrivePower.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Коэффициент загрузки привода ГПА</td><td>" + result.driveLoadFactor.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Расход топливного газа на КЦ</td><td>" + result.fuelGasConsumption.toFixed(2) + " (м^3/час)</td></tr>");
        sb.push("<tr><td>Расход электроэнергии ГПА</td><td>" + result.electricityConsumption.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Коммерческая производительность ГПА</td><td>" + result.commercialPerformance.toFixed(2) + " (м^3/час)</td></tr>");
    } else if (language === "kk") {
        sb.push("<h1>Нәтиже</h1>");
        sb.push("<table>");
        sb.push("<tr><th>Сипаттамасы</th><th>Мәні</th></tr>");
        sb.push("<tr><td>Компрессордың шығысындағы газ қысымы</td><td>" + result.gasPressureAtTheBlowerOutlet.toFixed(2) + " (кг/см^2)</td></tr>");
        sb.push("<tr><td>Компрессордың шығысындағы газ температурасы</td><td>" + result.gasTemperatureAtTheBlowerOutlet.toFixed(2) + " (°C)</td></tr>");
        sb.push("<tr><td>Компрессия коэффициенті</td><td>" + result.compressionRatio.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Қажетті айналым (ротордың қажетті жылдамдығы)</td><td>" + result.requiredRevolutionDesiredRotorSpeed.toFixed(2) + " (м^3/сағ)</td></tr>");
        sb.push("<tr><td>Кернеу аймағынан қашықтық коэффициенті</td><td>" + result.coefficientOfDistanceFromTheSurgeZone.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Ілініс қуаты (компрессорден тартылған қуат)</td><td>" + result.clutchPower.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Қолжетімді жетек қуаты</td><td>" + result.availableDrivePower.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Агрегат жетек блогының резервтік қуаты</td><td>" + result.reserveDrivePower.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Агрегаттың жүктеме коэффициенті</td><td>" + result.driveLoadFactor.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Отын газ шығыны</td><td>" + result.fuelGasConsumption.toFixed(2) + " (м^3/сағ)</td></tr>");
        sb.push("<tr><td>Агрегаттың қуат тұтынуы</td><td>" + result.electricityConsumption.toFixed(2) + " (кВт)</td></tr>");
        sb.push("<tr><td>Агрегаттың коммерциялық өнімділігі</td><td>" + result.commercialPerformance.toFixed(2) + " (м^3/сағ)</td></tr>");
    } else {
        sb.push("<h1>Result</h1>");
        sb.push("<table>");
        sb.push("<tr><th>Description</th><th>Meaning</th></tr>");
        sb.push("<tr><td>Gas pressure at the compressor outlet</td><td>" + result.gasPressureAtTheBlowerOutlet.toFixed(2) + " (kg/cm^2)</td></tr>");
        sb.push("<tr><td>Compressor outlet gas temperature</td><td>" + result.gasTemperatureAtTheBlowerOutlet.toFixed(2) + " (°C)</td></tr>");
        sb.push("<tr><td>Compression ratio</td><td>" + result.compressionRatio.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Required rotation (required rotor speed)</td><td>" + result.requiredRevolutionDesiredRotorSpeed.toFixed(2) + " (m^3/hour)</td></tr>");
        sb.push("<tr><td>The distance factor from the stress zone</td><td>" + result.coefficientOfDistanceFromTheSurgeZone.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Clutch power (power drawn from the compressor)</td><td>" + result.clutchPower.toFixed(2) + " (kW)</td></tr>");
        sb.push("<tr><td>Available drive power</td><td>" + result.availableDrivePower.toFixed(2) + " (kW)</td></tr>");
        sb.push("<tr><td>Reserve power of the aggregate drive unit</td><td>" + result.reserveDrivePower.toFixed(2) + " (kW)</td></tr>");
        sb.push("<tr><td>Unit load factor</td><td>" + result.driveLoadFactor.toFixed(2) + "</td></tr>");
        sb.push("<tr><td>Fuel gas consumption</td><td>" + result.fuelGasConsumption.toFixed(2) + " (m^3/hour)</td></tr>");
        sb.push("<tr><td>Power consumption of the unit</td><td>" + result.electricityConsumption.toFixed(2) + " (kW)</td></tr>");
        sb.push("<tr><td>Commercial performance of the unit</td><td>" + result.commercialPerformance.toFixed(2) + " (m^3/hour)</td></tr>");
    }

    sb.push("</table>");

    if (language === "ru") {
        sb.push("<div style='text-align: center; margin-top: 20px;'>");
        sb.push("<button type=\"button\" onclick=\"downloadData()\" style='padding: 10px 20px;'>Сохранить</button>");
        sb.push("</div>");
    } else if (language === "kk") {
        sb.push("<div style='text-align: center; margin-top: 20px;'>");
        sb.push("<button type=\"button\" onclick=\"downloadData()\" style='padding: 10px 20px;'>Сақтау</button>");
        sb.push("</div>");
    } else {
        sb.push("<div style='text-align: center; margin-top: 20px;'>");
        sb.push("<button type=\"button\" onclick=\"downloadData()\" style='padding: 10px 20px;'>Download</button>");
        sb.push("</div>");
    }

    sb.push("<script>");
    sb.push("function downloadData() {");
    sb.push("const htmlContent = document.documentElement.outerHTML;");
    sb.push("const blob = new Blob([htmlContent], { type: 'text/html' });");
    sb.push("const anchor = document.createElement('a');");
    sb.push("anchor.href = URL.createObjectURL(blob);");
    sb.push("anchor.download = 'result.html';");
    sb.push("anchor.click();");
    sb.push("document.body.removeChild(anchor);");
    sb.push("URL.revokeObjectURL(anchor.href);");
    sb.push("}");
    sb.push("</script>");

    // sb.push("<script>");
    // sb.push("function downloadData() {");
    // sb.push("   const htmlContent = document.documentElement.outerHTML;");
    // sb.push("   var pdf = new jsPDF();");
    // sb.push("   pdf.html(htmlContent, {");
    // sb.push("       callback: function () {");
    // sb.push("       const filename = 'result.pdf';");
    // sb.push("       pdf.save(filename);");
    // sb.push("       }");
    // sb.push("   });");
    // sb.push("}");
    // sb.push("</script>");

    sb.push("</body>");
    sb.push("</html>");

    const html = sb.join("\n");

    const newWindow = window.open("", "_blank");
    newWindow.document.write(html);
    newWindow.document.close();
}
