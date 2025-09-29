document.addEventListener('DOMContentLoaded', function() {
    var btn = document.getElementById('exploreBtn');
    var heading = document.querySelector('.hero h1');
    btn.addEventListener('click', function(event) {
        event.preventDefault();
        heading.textContent = 'Welcome';
    });
});