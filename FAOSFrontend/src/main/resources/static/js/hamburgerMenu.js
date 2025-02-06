document.getElementById("hamburger-menu").addEventListener("click", function () {
    document.getElementById("nav-list").classList.toggle("active");
  });

  document.querySelectorAll(".dropdown-toggle").forEach((dropdown) => {
    dropdown.addEventListener("click", function (e) {
      e.preventDefault();
      this.parentElement.classList.toggle("active");
    });
  });