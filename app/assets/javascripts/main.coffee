jQuery ->
  $(window).on 'keypress', (event)->
    key = String.fromCharCode(event.which)
    switch key
      when 'j' then $($(".item img.icon:in-viewport").get(0)).parent(".item").next().trigger('click')
      when 'k' then $($(".item img.icon:in-viewport").get(0)).parent(".item").prev().trigger('click')

  $('ul.items').on
    click: (event)->
      if $(event.target).hasClass('item')
        unless $(event.target).hasClass('read')
          $(event.target).addClass('read')
          $.get('/item/' + $(event.target).attr('id').replace('item_', '') + '/read')
        $(':animated').stop()
        $(window).scrollTo $(event.target), 400,
          offset: - $('.navbar').height() - 10
