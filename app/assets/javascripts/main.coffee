jQuery ->
  window.switchStar = (element)->
    if $(element).hasClass('icon-star-empty')
      $(element).removeClass('icon-star-empty').addClass('icon-star')
    else
      $(element).removeClass('icon-star').addClass('icon-star-empty')

  $(window).on 'keypress', (event)->
    key = String.fromCharCode(event.which)
    switch key
      when 'j' then $('.items').find('.selected').next().trigger('click')
      when 'k' then $('.items').find('.selected'). prev().trigger('click')

  $('.items').on
    click: (event)->
      if $(event.target).hasClass('item')
        $('.items').find('.selected').removeClass('selected').addClass('read')
        $(event.target).addClass('selected')
        $(':animated').stop()
        $.get '/item/' + $(event.target).attr('id').replace('item_', ''), null, (data)->
          $('section.article').html(data)
          Hyphenator.run()

        $(window).scrollTo 0, 0

  $(document).on 'click', '.star i', (event)->
    event.preventDefault()
    $.get $(event.target).parent().attr('href')
    switchStar(event.target)
