jQuery ->
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
