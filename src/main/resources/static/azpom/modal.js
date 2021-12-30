   "use strict"
   let modal = document.getElementById("myModal");
   let images = document.getElementsByClassName("gallery");
   let modalImg = document.getElementById("img01");

   for(const image of images) {
	 image.onclick = function(){
	   modal.style.display = "block";
	   modalImg.src = this.src;
	 }
   };
   let span = document.getElementsByClassName("close")[0];
   span.onclick = function(){
	 modal.style.display = "none";
   }

/* TODO: Implement this and try to make it work for arrows on images.
   let nextImg = document.getElementsByClassName("gallery");
   let rightArrow = document.getElementsByClassName("cycleRight");
   let leftArrow = document.getElementsByClassName("cycleLeft");
   for(let i = 0; i < images.length; i++){
     rightArrow.onclick = function(){
	   modalImg.src = nextImg
     }
   }

  // function makeNavRelative() {

//     li.style.position = relative;
 //  }

   function expandNav() {
     let nav = document.getElementById("expand");
	 let mainHead = document.getElementsByClassName("expandClass");
	 let span = document.getElementById("threeLines");
	 if(nav.style.display == "none") {
	   nav.style.display = "initial";
	   nav.style.zIndex = "1";
	   nav.style.width = "135px";

	   mainHead[0].style.transform = "translate(-150px)";
	   mainHead[1].style.transform = "translate(-150px)";
	   span.style.transform = "translate(0px, 10px)";
	 } else {

	   nav.style.display = "none";
	   mainHead[0].style.transform = "translate(0px)";
	   mainHead[1].style.transform = "translate(0px)";
	   span.style.transform = "translate(0px, 0px)";
	 }
   }*/
