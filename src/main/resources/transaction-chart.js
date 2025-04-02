document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM loaded, checking for business category data');

    const businessCountData = getBusinessCountData();
    const businessSumData = getBusinessSumData();

    console.log('Business category count data:', businessCountData);
    console.log('Business category sum data:', businessSumData);

    if (businessCountData.length > 0) {
        createBusinessPieChart(businessCountData, 'business-account-chart');
    } else {
        console.error('No business category count data found');
    }

    if (businessSumData.length > 0) {
        createBusinessPieChart(businessSumData, 'business-sum-chart');
    } else {
        console.error('No business category sum data found');
    }
});

// Map every child to a JSON array
function getBusinessCountData() {
    const categoryElements = document.querySelectorAll('#payments-by-category .category-item');
    const data = [];

    categoryElements.forEach(element => {
        const category = element.querySelector('.category-name').textContent.replace(':', '').trim();
        const value = parseInt(element.querySelector('.category-count').textContent.trim());

        if (!isNaN(value)) {
            data.push({category, value});
        }
    });

    return data;
}

function getBusinessSumData() {
    const categoryElements = document.querySelectorAll('#payments-sum-by-category .category-item');
    const data = [];

    categoryElements.forEach(element => {
        const category = element.querySelector('.category-name').textContent.replace(':', '').trim();
        const valueText = element.querySelector('.category-sum').textContent.trim();
        const value = parseFloat(valueText.replace('£', '').replace(',', ''));

        if (!isNaN(value)) {
            data.push({category, value});
        }
    });

    return data;
}

function createBusinessPieChart(data, chartId) {
    const ctx = document.getElementById(chartId).getContext('2d');

    const labels = data.map(item => item.category);
    const values = data.map(item => item.value);
    const colors = generateColorPalette(data.length);

    Chart.register(ChartDataLabels);

    // Create pie charts using ChartJS
    // https://www.chartjs.org/docs/latest/charts/doughnut.html
    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                datalabels: {
                    display: true,
                    color: 'white',
                    font: {
                        size: 14,
                        weight: 'bold'
                    },
                    // Add a percentage label on each slice
                    formatter: (value, context) => {
                        let total = context.chart.getDatasetMeta(0).total;
                        let percentage = ((value / total) * 100).toFixed(1) + '%';
                        return percentage;
                    },
                    anchor: 'center',
                    align: 'center'
                },
                legend: {
                    labels: {
                        color: 'white'
                    }
                }
            }
        }
    });
}

// Create random colours
function generateColorPalette(count) {
    const colors = [];
    for (let i = 0; i < count; i++) {
        const hue = (i * 360 / count) % 360;
        colors.push(`hsla(${hue}, 70%, 60%, 0.7)`);
    }
    return colors;
}

console.log('Category elements found:', document.querySelectorAll('#payments-by-category .category-item').length);
console.log('Category elements found:', document.querySelectorAll('#payments-sum-by-category .category-item').length);