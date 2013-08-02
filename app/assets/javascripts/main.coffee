jQuery ->
  $('.dropdown-toggle').dropdown()
  $('.tools .info').popover({html: true, placement: 'bottom'})

  if (typeof document.hidden != "undefined")
    document.visibilityChange = "visibilitychange";
    document.visibilityState = "visibilityState";
  else if (typeof document.mozHidden != "undefined")
    document.visibilityChange = "mozvisibilitychange";
    document.visibilityState = "mozVisibilityState";
  else if (typeof document.msHidden != "undefined")
    document.visibilityChange = "msvisibilitychange";
    document.visibilityState = "msVisibilityState";
  else if (typeof document.webkitHidden != "undefined")
    document.visibilityChange = "webkitvisibilitychange";
    document.visibilityState = "webkitVisibilityState";


  # Mark read first item when tab is visible
  if (document[document.visibilityState] == "visible" && $(".items li").eq(0).hasClass("read") == false)
    if $(".items li").length > 0
      $.get '/item/' + $(".items li").eq(0).attr('id').replace('item_', '')

  $(document).on document.visibilityChange, (event)->
    if (document[document.visibilityState] == "visible" && $(".items li").eq(0).hasClass("read") == false)
      if $(".items li").length > 0
        $.get '/item/' + $(".items li").eq(0).attr('id').replace('item_', '')

  window.switchStar = (element)->
    if $(element).hasClass('icon-star-empty')
      $(element).removeClass('icon-star-empty').addClass('icon-star')
    else
      $(element).removeClass('icon-star').addClass('icon-star-empty')

  $(window).on 'keypress', (event)->
    key = String.fromCharCode(event.which)
    switch key
      when 'j'
        $('.items').find('.selected').next().trigger('click')
        if $(".items").position().top + $('.items').find('.selected').position().top + $('.items').find('.selected').height() > $(window).height()
          $('.items').scrollTo $('.items').find('.selected'), 500
      when 'k'
        $('.items').find('.selected').prev().trigger('click')
        if $('.items').find('.selected').position().top < 0
          $('.items').scrollTo $('.items').find('.selected'), 100

  $('.items').on
    click: (event)->
      if $(event.target).hasClass('item')
        loadItem($(event.target).attr('id'))

  $(document).on 'click', '.star i', (event)->
    event.preventDefault()
    $.get $(event.target).parent().attr('href')
    switchStar(event.target)

$('.unread .number').html($(".item").not(".read").length)
$('.unread').css('display', 'block')

$('#expandSearch').on 'click', (event)->
  $('#expandSearch').attr("style", "display: none !important")
  $('#search').css("display", "block")
  $('.unread').css('display', 'none')

if ($("#term").val().length > 0)
  $('#expandSearch').attr("style", "display: none !important")
  $('#search').css("display", "block")
  $('.unread').css('display', 'none')

jQuery.event.special.swipe.settings.threshold = 0.1

$(window).on
  movestart: (e)->
    if ((e.distX > e.distY && e.distX < -e.distY) || (e.distX < e.distY && e.distX > -e.distY))
      e.preventDefault();
  swipeleft: ()->
    if ($(window).width() <= 767)
      item = $('#' + $('h1').attr('id').replace('its_', '')).next()
      if (item.length > 0)
        loadItem(item.attr("id"))

  swiperight: ()->
    if ($(window).width() <= 767)
      item = $('#' + $('h1').attr('id').replace('its_', '')).prev()
      if (item.length > 0)
        loadItem(item.attr("id"))

loadItem = (itemId)->
  $('.items').find('.selected').removeClass('selected').addClass('read')
  $('#' + itemId).addClass('selected')
  id = itemId.replace('item_', '')
  $.get '/item/' + id, null, (data)->
    if (data.feed.unread == 0)
      $('#feed_' + data.feed.id + ' .unread').remove()
    else
      $('#feed_' + data.feed.id + ' .unread').text(data.feed.unread)


    if (data.feed.favicon != null)
      $('section.article .title .icon').eq(0)
        .css('background-image', 'url("' + data.feed.favicon + '")')
        .css('display', 'inline-block')
    else
      $('section.article .title .icon').eq(0)
        .css('display', 'none')

    $('h1').attr('id', 'its_item_' + id)

    $('section.article .title a').eq(0)
      .attr("href", data.item.url)
      .html(data.item.title)

    $('section.article .info').data('popover').options.content = "<i class='icon-time'></i> " + data.item.date

    if (data.bookmark == true)
      $('section.article .title .tools .star').eq(0)
        .attr('href', '/item/' + data.item.id + '/unstar')
        .find('i').attr('class', 'icon-star')
    else
      $('section.article .title .tools .star').eq(0)
        .attr('href', '/item/' + data.item.id + '/star')
        .find('i').attr('class', 'icon-star-empty')

    $('section.article .content').html(data.item.content)
    $('.unread .number').html($(".item").not(".read").length)

    Hyphenator.run()

  $(window).scrollTo 0, 0
