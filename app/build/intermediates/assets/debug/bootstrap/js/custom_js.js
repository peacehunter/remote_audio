
$("body").ready(function(){

  $('.click_class').click(function(){
  //  alert($(this).attr('href'));
    var id= $(this).attr('id');
    if(id!=null){
    $(".display_player").load(id);
    }

  });
});
