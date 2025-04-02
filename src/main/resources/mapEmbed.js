function getTopSpenderData() {
    const spenderElements = document.querySelectorAll('.insights-section .category-item');
    const data = [];

    // Map every child element of spenders to a JSON list
    spenderElements.forEach(element => {
        const name = element.querySelector('.spender-name').textContent.replace(':', '').trim();
        const postcode = element.querySelector('.spender-postcode').textContent.replace('(', '').replace(')', '').trim();
        const totalAmount = parseFloat(element.querySelector('.spender-amount').textContent.replace('£', '').replace(',', ''));

        if (!isNaN(totalAmount)) {
            data.push({ name, postcode, totalAmount });
        }
    });

    return data;
}

// Initialize Google Map
function initMap() {
    // Make sure map container exists before proceeding
    const mapContainer = document.getElementById('top-spenders-map');
    if (!mapContainer) {
        console.error("Map container not found!");
        return;
    }

    const topSpenders = getTopSpenderData();
    console.log("Top spenders data:", topSpenders);

    // Create map centered on Scotland
    const map = new google.maps.Map(mapContainer, {
        zoom: 7,
        center: { lat: 56.4907, lng: -4.2026 } // Center of Scotland
    });

    // Store geocoder requests
    const geocodingPromises = [];

    // Use Geocoding API to turn a postcode into a marker on a map
    // https://developers.google.com/maps/documentation/geocoding/start
    function geocodePostcode(postcode, name, amount) {
        return new Promise((resolve, reject) => {
            const geocoder = new google.maps.Geocoder();
            geocoder.geocode({ 'address': postcode + ', UK' }, function(results, status) {
                if (status === google.maps.GeocoderStatus.OK) {
                    resolve({
                        position: results[0].geometry.location,
                        name: name,
                        postcode: postcode,
                        amount: amount
                    });
                } else {
                    console.warn(`Geocoding failed for ${postcode}: ${status}`);
                    reject(status);
                }
            });
        });
    }

    // Create geocoding promises for each spender
    topSpenders.forEach(spender => {
        geocodingPromises.push(geocodePostcode(spender.postcode, spender.name, spender.totalAmount));
    });

    // Wait for all geocoding to complete
    Promise.all(geocodingPromises.map(p => p.catch(e => null)))
        .then(locations => {
            // Filter out any failed geocoding results
            const validLocations = locations.filter(loc => loc !== null);

            // Add markers for each location
            validLocations.forEach(loc => {
                const marker = new google.maps.Marker({
                    position: loc.position,
                    map: map,
                    title: loc.name
                });

                // Create info window for each marker
                const infowindow = new google.maps.InfoWindow({
                    content: `<div style="color: black;">
                        <strong>${loc.name}</strong><br>
                        Postcode: ${loc.postcode}<br>
                        Amount: £${loc.amount.toLocaleString()}
                    </div>`
                });

                // Add click listener to show info window
                marker.addListener('click', function() {
                    infowindow.open(map, marker);
                });
            });

            // Adjust map bounds to fit all markers
            if (validLocations.length > 0) {
                const bounds = new google.maps.LatLngBounds();
                validLocations.forEach(loc => {
                    bounds.extend(loc.position);
                });
                map.fitBounds(bounds);
            }
        })
        .catch(error => {
            console.error("Error creating map markers:", error);
        });
}

// Make sure the DOM is loaded before initializing the map
document.addEventListener("DOMContentLoaded", function() {
    console.log("DOM loaded, waiting for maps API to load");
});

// Add global initMap function for Google Maps callback
window.initMap = initMap;